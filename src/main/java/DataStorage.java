import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class DataStorage {
    @JsonProperty("tickets")
    private List<Ticket> tickets;

    public DataStorage() {
    }

    public DataStorage(List<Ticket> tickets) {
        this.tickets = tickets;
    }

    public List<Ticket> getTickets() {
        return tickets;
    }

    public void setTickets(List<Ticket> tickets) {
        this.tickets = tickets;
    }
}
