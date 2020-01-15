import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class TicketsStatistics {
    private List<Ticket> tickets;

    public TicketsStatistics(DataStorage dataStorage) {
        this(dataStorage.getTickets());
    }

    public TicketsStatistics(List<Ticket> tickets) {
        this.tickets = tickets;
    }

    public Duration getAverageTimeBetweenCities(String originName, String destinationName) {
        Duration average = Duration.ZERO;
        int k = 0;
        for (Ticket t : tickets) {
            if (t.getOriginName().equals(originName) && t.getDestinationName().equals(destinationName)) {
                LocalDateTime departure = LocalDateTime.of(t.getDepartureDate(), t.getDepartureTime());
                LocalDateTime arrival = LocalDateTime.of(t.getArrivalDate(), t.getArrivalTime());
                average = average.plus(Duration.between(departure, arrival));
                k++;
            }
        }
        return average.dividedBy(k);
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
    }
}
