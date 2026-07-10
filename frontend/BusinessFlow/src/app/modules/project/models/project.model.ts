export type ProjectStatus = 'PLANNING' | 'ACTIVE' | 'ON_HOLD' | 'COMPLETED' | 'CANCELLED';
export type Priority = 'LOW' | 'NORMAL' | 'HIGH' | 'URGENT';

export interface Project {
  id: number;
  name: string;
  description?: string;
  status: ProjectStatus;
  priority: Priority;
  ownerId?: number;
  ownerName?: string;
  startDate?: string;
  endDate?: string;
  progress: number;
  budget?: number;
}

export interface ProjectRequest {
  name: string;
  description?: string;
  status?: ProjectStatus;
  priority?: Priority;
  ownerId?: number;
  startDate?: string;
  endDate?: string;
  progress?: number;
  budget?: number;
}
