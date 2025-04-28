package com.example.own_example.models;

import java.util.ArrayList;
import java.util.List;

public class StudyGroup {
    private long id;
    private String leaderName;
    private long leaderId;
    private List<StudyGroupMember> members;
    private String groupName;

    private String status;

    public StudyGroup(long id, String leaderName, long leaderId, String groupName) {
        this.id = id;
        this.leaderName = leaderName;
        this.leaderId = leaderId;
        this.groupName = groupName;
        this.members = new ArrayList<>();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLeaderName() {
        return leaderName;
    }

    public void setLeaderName(String leaderName) {
        this.leaderName = leaderName;
    }

    public long getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(long leaderId) {
        this.leaderId = leaderId;
    }

    public List<StudyGroupMember> getMembers() {
        return members;
    }

    public void setMembers(List<StudyGroupMember> members) {
        this.members = members;
    }

    public void addMember(StudyGroupMember member) {
        if (this.members == null) {
            this.members = new ArrayList<>();
        }
        this.members.add(member);
    }

    public String getName() {
        return groupName;
    }

    public void setName(String name) {
        this.groupName = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getMemberCount() {
        return members != null ? members.size() + 1 : 1; // +1 for the leader
    }

    public String getMemberCountText() {
        return getMemberCount() + "/4 Members";
    }
}