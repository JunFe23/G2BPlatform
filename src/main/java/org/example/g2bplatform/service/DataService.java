package org.example.g2bplatform.service;

import org.example.g2bplatform.mapper.DataMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DataService {

    @Autowired
    private DataMapper dataMapper;
}
