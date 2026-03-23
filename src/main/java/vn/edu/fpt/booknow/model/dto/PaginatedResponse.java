package vn.edu.fpt.booknow.model.dto;

import java.util.List;


public class PaginatedResponse<T> {
    private List<T> data;
    private int currentPage;
    private int totalPages;
    private long totalItems;
    private int pageSize;
    private boolean hasNext;
    private boolean hasPrevious;
    private int startIndex;
    private int endIndex;

    // Constructor
    public PaginatedResponse(List<T> data, int currentPage, int totalPages, long totalItems, int pageSize) {
        this.data = data;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.totalItems = totalItems;
        this.pageSize = pageSize;
        this.hasNext = currentPage < totalPages;
        this.hasPrevious = currentPage > 1;
    }

    public PaginatedResponse(List<T> data, int currentPage, int totalPages, long totalItems, int pageSize, int startIndex, int endIndex) {
        this.data = data;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.totalItems = totalItems;
        this.pageSize = pageSize;
        this.hasNext = currentPage < totalPages;
        this.hasPrevious = currentPage > 1;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }

    // Getters
    public List<T> getData() {
        return data;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public long getTotalItems() {
        return totalItems;
    }

    public int getPageSize() {
        return pageSize;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public boolean isHasPrevious() {
        return hasPrevious;
    }

    // Setters
    public void setData(List<T> data) {
        this.data = data;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public void setTotalItems(long totalItems) {
        this.totalItems = totalItems;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }

    public void setHasPrevious(boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }

    @Override
    public String toString() {
        return "PaginatedResponse{" +
                "data=" + data +
                ", currentPage=" + currentPage +
                ", totalPages=" + totalPages +
                ", totalItems=" + totalItems +
                ", pageSize=" + pageSize +
                ", hasNext=" + hasNext +
                ", hasPrevious=" + hasPrevious +
                '}';
    }
}
