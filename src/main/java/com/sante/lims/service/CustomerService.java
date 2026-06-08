package com.sante.lims.service;

import com.sante.lims.dao.CustomerModuleRepository;
import com.sante.lims.model.CustomerProfile;
import com.sante.lims.model.NotificationItem;
import com.sante.lims.model.TestCatalogItem;
import com.sante.lims.model.TestRequest;

import java.sql.SQLException;
import java.util.List;

public class CustomerService {
    private final CustomerModuleRepository repository = new CustomerModuleRepository();

    public CustomerProfile getProfile(long customerId) throws SQLException {
        return repository.getCustomerProfile(customerId);
    }

    public List<TestCatalogItem> getTestCatalog() throws SQLException {
        return repository.getAvailableTests();
    }

    public long placeOrder(long customerId, long testId) throws SQLException {
        return repository.createTestRequest(customerId, testId);
    }

    public List<TestRequest> getActiveRequests(long customerId) throws SQLException {
        return repository.getActiveRequests(customerId);
    }

    public List<TestRequest> getRequestHistory(long customerId) throws SQLException {
        return repository.getRequestHistory(customerId);
    }

    public List<NotificationItem> getNotifications(long customerId) throws SQLException {
        return repository.getNotifications(customerId);
    }
}
