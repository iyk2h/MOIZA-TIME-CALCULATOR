package com.ll.moizatimecalculator.boundedContext.selectedTime.controller;

import com.ll.moizatimecalculator.boundedContext.room.entity.Room;
import com.ll.moizatimecalculator.boundedContext.room.service.RoomService;
import com.ll.moizatimecalculator.boundedContext.selectedTime.service.SelectedTimeService;
import com.ll.moizatimecalculator.boundedContext.selectedTime.service.TimeRangeWithMember;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SelectedTimeController {
    private final SelectedTimeService selectedTimeService;

    private final RoomService roomService;


    @GetMapping("/selected-time/{roomId}")
    public List<TimeRangeWithMember> overlappingTimes(@PathVariable Long roomId){
        Room room = roomService.getRoom(roomId);
        return selectedTimeService.findOverlappingTimeRanges(room);
    }
}
