package kz.halyqsoft.univercity.entity.beans.univercity.view;

import org.r3a.common.entity.AbstractEntity;
import org.r3a.common.entity.EFieldType;
import org.r3a.common.entity.FieldInfo;

public class VDepartmentInfo extends AbstractEntity {

    @FieldInfo(type=EFieldType.TEXT, order = 1)
    private String deptName;

    @FieldInfo(type = EFieldType.INTEGER, order = 2)
    private Long count;

    @FieldInfo(type = EFieldType.INTEGER, order = 3)
    private Long isPresent;

    @FieldInfo(type = EFieldType.DOUBLE, order = 4)
    private double percantage;

    @FieldInfo(type = EFieldType.INTEGER, order = 5, inGrid = false, inView = false, inEdit = false)
    private Long departmentID;

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public double getPercantage() {
        return percantage;
    }

    public void setPercantage(double percantage) {
        this.percantage = percantage;
    }

    public Long getIsPresent() {
        return isPresent;
    }

    public void setIsPresent(Long isPresent) {
        this.isPresent = isPresent;
    }

    public Long getDepartmentID() {
        return departmentID;
    }

    public void setDepartmentID(Long departmentID) {
        this.departmentID = departmentID;
    }
}