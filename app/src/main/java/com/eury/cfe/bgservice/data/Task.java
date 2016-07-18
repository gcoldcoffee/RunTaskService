package com.eury.cfe.bgservice.data;

import java.io.Serializable;

/**
 * Created by aoe on 2016/5/9.
 */
public class Task implements Serializable {

    public Task(){
    }


    /**
     * 任务时长
     */
    public Integer taskTime;

    /**
     * 任务包名
     */
    public String packName;

    public Task(String packName,Integer taskTime){
        this.packName=packName;
        this.taskTime=taskTime;
    }

    public Integer getTaskTime() {
        return taskTime;
    }

    public void setTaskTime(Integer taskTime) {
        this.taskTime = taskTime;
    }

    public String getPackName() {
        return packName;
    }

    public void setPackName(String packName) {
        this.packName = packName;
    }
}
