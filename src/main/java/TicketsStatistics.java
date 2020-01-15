import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class TicketsStatistics {
    private List<Ticket> tickets;

    public TicketsStatistics(DataStorage dataStorage) {
        this(dataStorage.getTickets());
    }

    public TicketsStatistics(List<Ticket> tickets) {
        this.tickets = tickets;
    }

    public static void main(String[] args) throws IOException {
        String departureCity = "Владивосток";
        String arrivalCity = "Тель-Авив";
        int percentile = 90;

        if (args.length >= 2) {
            departureCity = args[0];
            arrivalCity = args[1];

            if (args.length == 3) {
                int p = Integer.parseInt(args[2]);
                if (p > 0 && p <= 100) {
                    percentile = p;
                }
            }
        }
        else if (args.length == 1) {
            int p = Integer.parseInt(args[2]);
            if (p > 0 && p <= 100) {
                percentile = p;
            }
        }

        System.out.println(String.format("Departure city: %s, Arrival city: %s, Percentile: %d" + System.lineSeparator(),
                departureCity, arrivalCity, percentile));

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        DataStorage dataStorage = mapper.readValue(new File("tickets.json"), DataStorage.class);

        TicketsStatistics ticketsStatistics = new TicketsStatistics(dataStorage);

        long averageMinutes = ticketsStatistics.
                getAverageTimeBetweenCities(departureCity, arrivalCity)
                .toMinutes();
        System.out.println(String.format("Average duration between %s and %s: %dh %dm",
                departureCity, arrivalCity, averageMinutes / 60, averageMinutes % 60));

        long percentileMinutes = ticketsStatistics
                .getFlightTimePercentileBetweenCities(percentile, departureCity, arrivalCity)
                .toMinutes();
        System.out.println(String.format("%d percentil of flight time between %s and %s: %dh %dm",
                percentile, departureCity, arrivalCity, percentileMinutes / 60, percentileMinutes % 60));
    }

    public Duration getAverageTimeBetweenCities(String departureCity, String arrivalCity) {
        Duration average = Duration.ZERO;
        int k = 0;
        for (Ticket t : tickets) {
            if (t.getOriginName().equals(departureCity) && t.getDestinationName().equals(arrivalCity)) {
                average = average.plus(calculateFlightTime(t));
                k++;
            }
        }
        return average.dividedBy(k);
    }

    public Duration getFlightTimePercentileBetweenCities(int percentile, String departureCity, String arrivalCity) {

        List<Ticket> ticketsByFlightTime = tickets.stream().filter(t -> t.getOriginName().equals(departureCity)).filter(t -> t.getDestinationName().equals(arrivalCity)).distinct().collect(Collectors.toList());
        ticketsByFlightTime.sort((o1, o2) -> (int) (calculateFlightTime(o1).toMinutes() - calculateFlightTime(o2).toMinutes()));
        double koef = (double) percentile / 100 * ticketsByFlightTime.size();
        int index = (int) Math.ceil(koef) - 1;

        return calculateFlightTime(ticketsByFlightTime.get(index));
    }

    private Duration calculateFlightTime(Ticket ticket) {
        LocalDateTime departure = LocalDateTime.of(ticket.getDepartureDate(), ticket.getDepartureTime());
        LocalDateTime arrival = LocalDateTime.of(ticket.getArrivalDate(), ticket.getArrivalTime());

        return Duration.between(departure, arrival);
    }
}
