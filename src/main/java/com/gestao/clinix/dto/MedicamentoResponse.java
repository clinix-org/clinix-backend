package com.gestao.clinix.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class MedicamentoResponse {
    private List<MedicamentoDTO> medicamentos;
    private Integer pageSize;
    private Integer pageNumber;
    private Long totalElements;
    private Integer totalPages;
    private boolean lastPage;
    private LocalDateTime timestamp;

    public MedicamentoResponse() {
    }

    public MedicamentoResponse(
            List<MedicamentoDTO> medicamentos,
            Integer pageSize,
            Integer pageNumber,
            Long totalElements,
            Integer totalPages,
            boolean lastPage,
            LocalDateTime timestamp) {
        this.medicamentos = medicamentos;
        this.pageSize = pageSize;
        this.pageNumber = pageNumber;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.lastPage = lastPage;
        this.timestamp = timestamp;
    }

    public List<MedicamentoDTO> getMedicamentos() {
        return medicamentos;
    }

    public void setMedicamentos(List<MedicamentoDTO> medicamentos) {
        this.medicamentos = medicamentos;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }

    public Long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(Long totalElements) {
        this.totalElements = totalElements;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public boolean isLastPage() {
        return lastPage;
    }

    public void setLastPage(boolean lastPage) {
        this.lastPage = lastPage;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        MedicamentoResponse that = (MedicamentoResponse) o;
        return lastPage == that.lastPage && Objects.equals(pageSize, that.pageSize) && Objects.equals(pageNumber, that.pageNumber) && Objects.equals(totalElements, that.totalElements) && Objects.equals(totalPages, that.totalPages) && Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pageSize, pageNumber, totalElements, totalPages, lastPage, timestamp);
    }
}
