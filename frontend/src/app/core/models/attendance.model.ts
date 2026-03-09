/**
 * Matches AttendanceDto from the backend.
 * Also used as the WebSocket broadcast payload on /topic/attendance.
 */
export interface AttendanceRecord {
  id:                number;
  employeId:         number;
  employeNomComplet: string;
  type:              'IN' | 'OUT';
  clockedAt:         string;   // ISO Instant
  confidence?:       number;
  cameraId?:         string;
  siteId?:           string;
  createdAt?:        string;
}

/** Alias — the WS broadcast payload IS AttendanceDto. */
export type AttendanceEvent = AttendanceRecord;
