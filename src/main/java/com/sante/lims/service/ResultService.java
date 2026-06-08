package com.sante.lims.service;

import com.sante.lims.dao.CustomerModuleRepository;
import com.sante.lims.model.LabResult;

import java.sql.SQLException;
import java.util.List;

public class ResultService {
    private final CustomerModuleRepository repository = new CustomerModuleRepository();

    public List<LabResult> getValidatedResults(long customerId) throws SQLException {
        return repository.getValidatedResults(customerId);
    }
}
