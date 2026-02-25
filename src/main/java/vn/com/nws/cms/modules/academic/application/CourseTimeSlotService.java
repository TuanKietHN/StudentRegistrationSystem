package vn.com.nws.cms.modules.academic.application;

import vn.com.nws.cms.modules.academic.api.dto.CourseTimeSlotRequest;
import vn.com.nws.cms.modules.academic.api.dto.CourseTimeSlotResponse;

import java.util.List;

public interface CourseTimeSlotService {
    List<CourseTimeSlotResponse> getCourseTimeSlots(Long courseId);
    List<CourseTimeSlotResponse> replaceCourseTimeSlots(Long courseId, List<CourseTimeSlotRequest> requests);
}

