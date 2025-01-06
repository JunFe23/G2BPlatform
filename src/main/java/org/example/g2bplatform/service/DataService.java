package org.example.g2bplatform.service;

import org.example.g2bplatform.mapper.DataMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DataService {

    @Autowired
    private DataMapper dataMapper;

    public List<Map<String, String>> getThingsData(int start, int length, String dminsttNm, String dminsttNmDetail, String prdctClsfcNo, String cntctCnclsMthdNm, String firstCntrctDate, Integer year, String month, String rangeStart, String rangeEnd) {
        return dataMapper.getThingsData(start, length, dminsttNm, dminsttNmDetail, prdctClsfcNo, cntctCnclsMthdNm, firstCntrctDate, year, month, rangeStart, rangeEnd);
    }

    public int getThingsTotalCount(String category) {
        return dataMapper.getThingsTotalCount(category);
    }

    public int getThingsFilteredCount(String dminsttNm, String dminsttNmDetail, String prdctClsfcNo, String cntctCnclsMthdNm, String firstCntrctDate, Integer year, String month, String rangeStart, String rangeEnd) {
        return dataMapper.getThingsFilteredCount(dminsttNm, dminsttNmDetail, prdctClsfcNo, cntctCnclsMthdNm, firstCntrctDate, year, month, rangeStart, rangeEnd);
    }

    public List<Map<String, String>> getServicesData(int start, int length, String search, String category) {
        return dataMapper.getServicesData(start, length, search, category);
    }

    public int getServicesTotalCount(String category) {
        return dataMapper.getServicesTotalCount(category);
    }

    public int getServicesFilteredCount(String search, String category) {
        return dataMapper.getServicesFilteredCount(search, category);
    }

    public List<Map<String, String>> getConstructionsData(int start, int length, String search, String category) {
        return dataMapper.getConstructionsData(start, length, search, category);
    }

    public int getConstructionsTotalCount(String category) {
        return dataMapper.getConstructionsTotalCount(category);
    }

    public int getConstructionsFilteredCount(String search, String category) {
        return dataMapper.getConstructionsFilteredCount(search, category);
    }


    public List<Map<String, String>> getTopsData(int start, int length, String search, String category) {
        return dataMapper.getTopsData(start, length, search, category);
    }

    public int getTopsTotalCount(String category) {
        return dataMapper.getTopsTotalCount(category);
    }

    public int getTopsFilteredCount(String search, String category) {
        return dataMapper.getTopsFilteredCount(search, category);
    }
}
