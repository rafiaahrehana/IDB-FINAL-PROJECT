export interface Meeting {
  id: number;
  title: string;
  description?: string;
  organizerId?: number;
  startTime?: string;
  endTime?: string;
  location?: string;
}

export interface MeetingRequest {
  title: string;
  description?: string;
  organizerId?: number;
  startTime?: string;
  endTime?: string;
  location?: string;
}
