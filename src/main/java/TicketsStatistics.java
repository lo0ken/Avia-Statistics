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
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        DataStorage dataStorage = mapper.readValue(new File("tickets.json"), DataStorage.class);
        dataStorage.getTickets().forEach(System.out::println);

        TicketsStatistics ticketsStatistics = new TicketsStatistics(dataStorage);
        long minutes = ticketsStatistics.getAverageTimeBetweenCities("Владивосток", "Тель-Авив").toMinutes();
        System.out.println(String.format("Average duration between Владивосток and Тель-Авив: %dh %dm",
                minutes / 60, minutes % 60));

        long percentileTime = ticketsStatistics.getFlightTimePercentileBetweenCities(90, "Владивосток", "Тель-Авив").toMinutes();
        System.out.println(String.format("%d percentil of flight time between Владивосток and Тель-Авив: %dh %dm", 90, percentileTime / 60, percentileTime % 60));
    }

    public Duration getAverageTimeBetweenCities(String departureCity, String arrivalCity) {
        Duration average = Duration.ZERO;
        int k = 0;
        for (Ticket t : tickets) {
            if (t.getOriginName().equals(departureCity) && t.getDestinationName().equals(arrivalCity)) {
                System.out.println(calculateFlightTime(t).toMinutes());
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
