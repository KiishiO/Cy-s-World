package com.example.own_example;

import java.util.ArrayList;
import java.util.List;

public class StudyGroup {
    private String id;
    private String name;
    private List<StudyGroupMember> members;
    private int maxMembers;

    public StudyGroup() {
        this.members = new ArrayList<>();
        this.maxMembers = 4; // Default max members
    }

    public StudyGroup(String id, String name) {
        this.id = id;
        this.name = name;
        this.members = new ArrayList<>();
        this.maxMembers = 4;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<StudyGroupMember> getMembers() {
        return members;
    }

    public void setMembers(List<StudyGroupMember> members) {
        this.members = members;
    }

    public void addMember(StudyGroupMember member) {
        if (members.size() < maxMembers) {
            this.members.add(member);
        }
    }

    public boolean removeMember(String memberId) {
        return members.removeIf(member -> member.getId().equals(memberId));
    }

    public int getMemberCount() {
        return members.size();
    }

    public int getMaxMembers() {
        return maxMembers;
    }

    public void setMaxMembers(int maxMembers) {
        this.maxMembers = maxMembers;
    }

    public boolean isFull() {
        return members.size() >= maxMembers;
    }
}