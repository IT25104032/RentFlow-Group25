package com.compulin.rentflow.dto.module4;

import java.util.List;

public class ProcessReturnResponse {

    private Integer returnId;
    private Integer rentalId;
    private String returnType;
    private String rentalStatus;
    private List<Integer> damagedReturnItemIds;

    public ProcessReturnResponse() {
    }

    public ProcessReturnResponse(
            Integer returnId,
            Integer rentalId,
            String returnType,
            String rentalStatus,
            List<Integer> damagedReturnItemIds) {

        this.returnId = returnId;
        this.rentalId = rentalId;
        this.returnType = returnType;
        this.rentalStatus = rentalStatus;
        this.damagedReturnItemIds = damagedReturnItemIds;
    }

    public Integer getReturnId() {
        return returnId;
    }

    public void setReturnId(Integer returnId) {
        this.returnId = returnId;
    }

    public Integer getRentalId() {
        return rentalId;
    }

    public void setRentalId(Integer rentalId) {
        this.rentalId = rentalId;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public String getRentalStatus() {
        return rentalStatus;
    }

    public void setRentalStatus(String rentalStatus) {
        this.rentalStatus = rentalStatus;
    }

    public List<Integer> getDamagedReturnItemIds() {
        return damagedReturnItemIds;
    }

    public void setDamagedReturnItemIds(List<Integer> damagedReturnItemIds) {
        this.damagedReturnItemIds = damagedReturnItemIds;
    }
}