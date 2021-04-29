package data;

import java.time.LocalDateTime;

public class SubOrder {

    private final Integer OrderNumber;
    private final String OrderType;
    private final String state;
    private final Integer id;
    private final Integer px;
    private final Integer py;
    private final Integer quantity;
    private final LocalDateTime entryTime;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final LocalDateTime maxFinishingTime;
    private final Long timeLeft;
    private final Integer nbeing;
    private final Integer npending;
    private final Integer nfinished;

    public SubOrder(
            int orderNumber,
            String orderType,
            String state,
            int px,
            int py,
            int quantity,
            LocalDateTime maxFinishingTime,
            LocalDateTime entryTime,
            int id
    ) {
        this.OrderNumber = orderNumber;
        this.OrderType = orderType;
        this.state = state;
        this.px = px;
        this.py = py;
        this.quantity = quantity;
        this.maxFinishingTime = maxFinishingTime;
        this.entryTime = entryTime;
        this.id = id;
        this.startTime = null;
        this.endTime = null;
        this.timeLeft = 5_000_000L;
        nbeing = null;
        npending = null;
        nfinished = null;
    }

    public SubOrder(
            int orderNumber,
            String orderType,
            String state,
            int px,
            int py,
            int quantity,
            LocalDateTime maxFinishingTime,
            LocalDateTime entryTime,
            LocalDateTime startTime,
            long timeLeft,
            int id
    ) {
        this.OrderNumber = orderNumber;
        this.OrderType = orderType;
        this.state = state;
        this.px = px;
        this.py = py;
        this.quantity = quantity;
        this.maxFinishingTime = maxFinishingTime;
        this.entryTime = entryTime;
        this.startTime = startTime;
        this.timeLeft = timeLeft;
        this.id = id;
        this.endTime = null;
        nbeing = null;
        npending = null;
        nfinished = null;
    }

    public SubOrder(
            int orderNumber,
            String orderType,
            String state,
            int px,
            int py,
            int quantity,
            LocalDateTime maxFinishingTime,
            LocalDateTime entryTime,
            LocalDateTime startTime,
            LocalDateTime endTime,
            long timeLeft,
            int id
    ) {
        this.OrderNumber = orderNumber;
        this.OrderType = orderType;
        this.state = state;
        this.px = px;
        this.py = py;
        this.quantity = quantity;
        this.maxFinishingTime = maxFinishingTime;
        this.entryTime = entryTime;
        this.startTime = startTime;
        this.endTime = endTime;
        this.timeLeft = timeLeft;
        this.id = id;
        nbeing = null;
        npending = null;
        nfinished = null;
    }

    public SubOrder(
            int orderNumber,
            String orderType,
            String state,
            LocalDateTime entryTime,
            LocalDateTime startTime,
            LocalDateTime endTime,
            int npending,
            int nbeing,
            int nfinished,
            Long timeLeft
    ) {
        this.OrderNumber = orderNumber;
        this.OrderType = orderType;
        this.state = state;
        this.entryTime = entryTime;
        this.startTime = startTime;
        this.endTime = endTime;
        this.timeLeft = timeLeft;
        this.nbeing = nbeing;
        this.npending = npending;
        this.nfinished = nfinished;
        this.id = null;
        this.px = null;
        this.py = null;
        this.quantity = null;
        this.maxFinishingTime = null;
    }

    public Integer getPx() { return px; }

    public Integer getPy() { return py; }

    public LocalDateTime getMaxFinishingTime() { return maxFinishingTime; }

    public String getState() {
        return state;
    }

    public Integer getOrderNumber() {
        return OrderNumber;
    }

    public Integer getQuantity() { return quantity; }

    public Integer getId() { return id; }

    public LocalDateTime getEntryTime() { return entryTime; }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() { return endTime; }

    public String getOrderType() { return OrderType; }

    public Long getTimeLeft() { return timeLeft; }

    public Integer getNbeing() { return nbeing; }

    public Integer getNpending() { return npending; }

    public Integer getNfinished() { return nfinished; }
}
