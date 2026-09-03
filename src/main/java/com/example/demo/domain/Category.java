package com.example.demo.domain;

public class Category {

    private Integer id;
    private Integer parentId;
    private String name;
    private String nameAll;

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public Integer getParentId() {
        return parentId;
    }
    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getNameAll() {
        return nameAll;
    }
    public void setNameAll(String nameAll) {
        this.nameAll = nameAll;
    }
    public Category(Integer id, Integer parentId, String name, String nameAll) {
        this.id = id;
        this.parentId = parentId;
        this.name = name;
        this.nameAll = nameAll;
    }

    public Category(){}
}
