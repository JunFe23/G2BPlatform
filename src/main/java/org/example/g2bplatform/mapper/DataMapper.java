package org.example.g2bplatform.mapper;

import lombok.Data;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DataMapper {
    String getDataById(int id);
}
