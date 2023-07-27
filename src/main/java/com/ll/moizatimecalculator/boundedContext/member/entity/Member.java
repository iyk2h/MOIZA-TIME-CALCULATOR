package com.ll.moizatimecalculator.boundedContext.member.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ll.moizatimecalculator.base.entity.BaseEntity;
import com.ll.moizatimecalculator.boundedContext.room.entity.EnterRoom;
import com.ll.moizatimecalculator.boundedContext.room.entity.Room;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
public class Member extends BaseEntity implements Comparable<Member>{
    @Column(unique = true)
    private String name;
    private String email;
    private String profile;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "member")
    @ToString.Exclude
    @JsonIgnore
    @Builder.Default
    private List<EnterRoom> enterRooms = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "leader")
    @ToString.Exclude
    @JsonIgnore
    @Builder.Default
    private List<Room> rooms = new ArrayList<>();

    @Override
    public int compareTo(Member o) {
        return name.compareTo(o.name);
    }
}
