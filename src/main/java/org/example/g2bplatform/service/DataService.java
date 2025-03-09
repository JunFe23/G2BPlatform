package org.example.g2bplatform.service;

import jakarta.transaction.Transactional;
import org.example.g2bplatform.mapper.DataMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DataService {

    @Autowired
    private DataMapper dataMapper;

    public List<Map<String, String>> getThingsData(int start, int length, String dminsttNm, String dminsttNmDetail, String prdctClsfcNo, String cntctCnclsMthdNm, String firstCntrctDate, Integer year, String month, String rangeStart, String rangeEnd, int showSavedOnly) {
        return dataMapper.getThingsData(start, length, dminsttNm, dminsttNmDetail, prdctClsfcNo, cntctCnclsMthdNm, firstCntrctDate, year, month, rangeStart, rangeEnd, showSavedOnly);
    }

    public int getThingsTotalCount(String category) {
        return dataMapper.getThingsTotalCount(category);
    }

    public int getThingsFilteredCount(String dminsttNm, String dminsttNmDetail, String prdctClsfcNo, String cntctCnclsMthdNm, String firstCntrctDate, Integer year, String month, String rangeStart, String rangeEnd, int showSavedOnly) {
        return dataMapper.getThingsFilteredCount(dminsttNm, dminsttNmDetail, prdctClsfcNo, cntctCnclsMthdNm, firstCntrctDate, year, month, rangeStart, rangeEnd, showSavedOnly);
    }

    public List<Map<String, String>> getServicesData(int start, int length, String dminsttNm, String dminsttNmDetail, String prdctClsfcNo, String cntctCnclsMthdNm, String firstCntrctDate, Integer year, String month, String rangeStart, String rangeEnd, int showSavedOnly) {
        return dataMapper.getServicesData(start, length, dminsttNm, dminsttNmDetail, prdctClsfcNo, cntctCnclsMthdNm, firstCntrctDate, year, month, rangeStart, rangeEnd, showSavedOnly);
    }

    public int getServicesTotalCount(String category) {
        return dataMapper.getServicesTotalCount(category);
    }

    public int getServicesFilteredCount(int start, int length, String dminsttNm, String dminsttNmDetail, String prdctClsfcNo, String cntctCnclsMthdNm, String firstCntrctDate, Integer year, String month, String rangeStart, String rangeEnd, int showSavedOnly) {
        return dataMapper.getServicesFilteredCount(start, length, dminsttNm, dminsttNmDetail, prdctClsfcNo, cntctCnclsMthdNm, firstCntrctDate, year, month, rangeStart, rangeEnd, showSavedOnly);
    }

    public List<Map<String, String>> getConstructionsData(int start, int length, String dminsttNm, String dminsttNmDetail, String prdctClsfcNo, String cntctCnclsMthdNm, String firstCntrctDate, Integer year, String month, String rangeStart, String rangeEnd, int showSavedOnly) {
        return dataMapper.getConstructionsData(start, length, dminsttNm, dminsttNmDetail, prdctClsfcNo, cntctCnclsMthdNm, firstCntrctDate, year, month, rangeStart, rangeEnd, showSavedOnly);
    }

    public int getConstructionsTotalCount(String category) {
        return dataMapper.getConstructionsTotalCount(category);
    }

    public int getConstructionsFilteredCount(int start, int length, String dminsttNm, String dminsttNmDetail, String prdctClsfcNo, String cntctCnclsMthdNm, String firstCntrctDate, Integer year, String month, String rangeStart, String rangeEnd, int showSavedOnly) {
        return dataMapper.getConstructionsFilteredCount(start, length, dminsttNm, dminsttNmDetail, prdctClsfcNo, cntctCnclsMthdNm, firstCntrctDate, year, month, rangeStart, rangeEnd, showSavedOnly);
    }


    public List<Map<String, String>> getTopsData(String type, int start, int length, String dminsttNm, String dminsttNmDetail, String prdctClsfcNo, String cntctCnclsMthdNm, String firstCntrctDate, Integer year, String month, String rangeStart, String rangeEnd, int showSavedOnly) {
        return dataMapper.getTopsData(type, start, length, dminsttNm, dminsttNmDetail, prdctClsfcNo, cntctCnclsMthdNm, firstCntrctDate, year, month, rangeStart, rangeEnd, showSavedOnly);
    }

    public int getTopsTotalCount(String category) {
        return dataMapper.getTopsTotalCount(category);
    }

    public int getTopsFilteredCount(String type, int start, int length, String dminsttNm, String dminsttNmDetail, String prdctClsfcNo, String cntctCnclsMthdNm, String firstCntrctDate, Integer year, String month, String rangeStart, String rangeEnd, int showSavedOnly) {
        return dataMapper.getTopsFilteredCount(type, start, length, dminsttNm, dminsttNmDetail, prdctClsfcNo, cntctCnclsMthdNm, firstCntrctDate, year, month, rangeStart, rangeEnd, showSavedOnly);
    }

    @Transactional
    public void updateCheckedThings(int id, boolean checked) {
        int checkedValue = checked ? 1 : 0; // true -> 1, false -> 0
        int updatedRows = dataMapper.updateCheckedThings(id, checkedValue);
        if (updatedRows == 0) {
            throw new RuntimeException("업데이트에 실패했습니다. ID: " + id);
        }
    }

    @Transactional
    public void updateCheckedServices(int id, boolean checked) {
        int checkedValue = checked ? 1 : 0; // true -> 1, false -> 0
        int updatedRows = dataMapper.updateCheckedServices(id, checkedValue);
        if (updatedRows == 0) {
            throw new RuntimeException("업데이트에 실패했습니다. ID: " + id);
        }
    }

    @Transactional
    public void updateCheckedConstructions(int id, boolean checked) {
        int checkedValue = checked ? 1 : 0; // true -> 1, false -> 0
        int updatedRows = dataMapper.updateCheckedConstructions(id, checkedValue);
        if (updatedRows == 0) {
            throw new RuntimeException("업데이트에 실패했습니다. ID: " + id);
        }
    }
}
