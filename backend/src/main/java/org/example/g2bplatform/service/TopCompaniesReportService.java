package org.example.g2bplatform.service;

import org.apache.ibatis.session.ResultHandler;
import org.example.g2bplatform.entity.TopManualContract;
import org.example.g2bplatform.mapper.TopCompaniesReportMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class TopCompaniesReportService {

    private final TopCompaniesReportMapper mapper;

    public TopCompaniesReportService(TopCompaniesReportMapper mapper) {
        this.mapper = mapper;
    }

    public List<Map<String, Object>> getList(Map<String, Object> p, boolean grouped) {
        return grouped ? mapper.selectGroupedList(p) : mapper.selectFlatList(p);
    }

    public int getCount(Map<String, Object> p, boolean grouped) {
        return grouped ? mapper.selectGroupedCount(p) : mapper.selectFlatCount(p);
    }

    public Map<String, Object> getTotals(Map<String, Object> p, boolean grouped) {
        return grouped ? mapper.selectGroupedTotals(p) : mapper.selectFlatTotals(p);
    }

    public void streamForExcel(Map<String, Object> p, boolean grouped,
                               ResultHandler<Map<String, Object>> handler) {
        if (grouped) {
            mapper.selectGroupedExport(p, handler);
        } else {
            mapper.selectFlatExport(p, handler);
        }
    }

    public List<Map<String, Object>> getCategoryHierarchy() {
        return mapper.selectCategoryHierarchy();
    }

    public List<String> getDistinctContractMethods() {
        return mapper.selectDistinctContractMethods();
    }

    // ===== 민수(직접입력) CRUD — 관리자 전용(컨트롤러에서 권한 체크) =====

    public List<TopManualContract> getManualList() {
        return mapper.selectManualList();
    }

    public TopManualContract getManual(Long id) {
        return mapper.selectManualById(id);
    }

    @Transactional
    public TopManualContract createManual(TopManualContract entity) {
        mapper.insertManual(entity);
        return mapper.selectManualById(entity.getId());
    }

    @Transactional
    public TopManualContract updateManual(Long id, TopManualContract entity) {
        entity.setId(id);
        mapper.updateManual(entity);
        return mapper.selectManualById(id);
    }

    @Transactional
    public boolean deleteManual(Long id) {
        return mapper.deleteManual(id) > 0;
    }
}
