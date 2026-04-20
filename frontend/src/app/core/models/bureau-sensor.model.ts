export interface BureauSensorDto {
  id: number;
  deviceId: string;
  name: string;
  location: string;
  sensorTypes: string[];
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface TemperatureReadingDto {
  id: number;
  sensorId: number;
  temperature: number;
  humidity: number;
  timestamp: string;
  createdAt: string;
}

export interface Co2ReadingDto {
  id: number;
  sensorId: number;
  co2Level: number;
  gasConcentration: number;
  alarmTriggered: boolean;
  timestamp: string;
  createdAt: string;
}

export interface OccupancyStatusDto {
  id: number;
  sensorId: number;
  isOccupied: boolean;
  motionDuration: number;
  confidenceLevel: number;
  timestamp: string;
  createdAt: string;
}

export interface SensorHealthDto {
  id: number;
  sensorId: number;
  uptimeSeconds: number;
  batteryLevel: number;
  signalStrength: number;
  errorCount: number;
  isOnline: boolean;
  lastHeartbeat: string;
  createdAt: string;
  updatedAt: string;
}

export interface SensorAlertDto {
  id: number;
  sensorId: number;
  alertType: string;
  thresholdValue: number;
  actualValue: number;
  isActive: boolean;
  triggeredAt: string;
  acknowledgedAt?: string;
  acknowledgedBy?: string;
  createdAt: string;
  updatedAt: string;
}

export interface BureauDashboardDto {
  id: number;
  sensor: BureauSensorDto;
  latestTemperature?: TemperatureReadingDto;
  latestCo2?: Co2ReadingDto;
  latestOccupancy?: OccupancyStatusDto;
  sensorHealth?: SensorHealthDto;
  temperatureHistory: TemperatureReadingDto[];
  co2History: Co2ReadingDto[];
  occupancyHistory: OccupancyStatusDto[];
  activeAlerts: SensorAlertDto[];
}
