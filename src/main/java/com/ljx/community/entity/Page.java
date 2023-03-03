package com.ljx.community.entity;

/*
*   封装分页相关信息
* */
public class Page {

    //当前页码
    private int currentpage = 1;

    //显示上限
    private int limit = 10;

    //数据总数(用于计算总的页数)
    private int rows;

    //查询路径（用于复用分页的链接）
    private String path;

    /*
        set方法要检查值是否符合逻辑
    * */

    public int getCurrentpage() {
        return currentpage;
    }

    public void setCurrentpage(int currentpage) {
        if(currentpage>1) {
            this.currentpage = currentpage;
        }
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        if(limit>=1 && limit <= 100){
            this.limit = limit;
        }
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        if(rows>=0) {
            this.rows = rows;
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /*获取当前页的其实行
    * 即 ：每一个页面有limit行，当前页面的起始行（第一行）为 (currentpage-1)*limit , 为什么不加1，从第0行开始计数
    * */
    public int getOffset(){
        return (currentpage - 1)*limit;
    }

    /*
    * 获取总页数
    *
    * */
    public int getTotal(){
        return (int)Math.ceil(1.0*rows/limit);
    }

    /*
    * 获取当前页的前面两页 当做界面上显示的起始页码
    *
    * */
    public int getFrom(){
        return Math.max(currentpage-2,1);
    }

    /*
    * 获取终止页码
    * */
    public int getTo(){
        return Math.min(currentpage+2,this.getTotal());
    }
}
