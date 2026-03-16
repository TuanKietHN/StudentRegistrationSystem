package vn.com.nws.cms.modules.academic.application;

import vn.com.nws.cms.modules.academic.api.dto.AttendanceMarkRequest;
import vn.com.nws.cms.modules.academic.api.dto.AttendanceSessionOpenRequest;
import vn.com.nws.cms.modules.academic.api.dto.AttendanceSessionResponse;
import vn.com.nws.cms.modules.academic.api.dto.AttendanceSessionRosterResponse;

import java.util.List;

public interface AttendanceService {
    AttendanceSessionResponse openSession(String username, boolean isAdmin, boolean isTeacher, AttendanceSessionOpenRequest request);
    List<AttendanceSessionResponse> listCohortSessions(Long cohortId, String username, boolean isAdmin, boolean isTeacher);
    AttendanceSessionRosterResponse getSessionRoster(Long sessionId, String username, boolean isAdmin, boolean isTeacher);
    AttendanceSessionRosterResponse markAttendance(Long sessionId, Long enrollmentId, String username, boolean isAdmin, boolean isTeacher, AttendanceMarkRequest request);
    boolean isExamBanned(Long cohortId, Long enrollmentId);
}
