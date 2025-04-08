package com.example.kitahack2025;

public class OrderModal {
    private String requestId;
    private String customerId;
    private String orderDate;
    private String status;
    private String orderPackedDate;

    public OrderModal() {}

    public OrderModal(String requestId, String customerId, String orderDate, String status) {
        this.requestId = requestId;
        this.customerId = customerId;
        this.orderDate = orderDate;
        this.status = status;
    }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getCustomerId() { return customerId; }

    public String getOrderDate() { return orderDate; }
    public String getOrderPackedDate() {
        return orderPackedDate;
    }

    public void setOrderPackedDate(String orderPackedDate) {
        this.orderPackedDate = orderPackedDate;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
