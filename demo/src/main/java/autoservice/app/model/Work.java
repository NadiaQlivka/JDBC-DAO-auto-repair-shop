package autoservice.app.model;

import java.time.LocalDate;

public class Work {
  private int id;
  private LocalDate dateWork;
  private int masterId;
  private int carId;
  private int serviceId;

  public Work(int id, LocalDate dateWork, int masterId, int carId, int serviceId) {
    this.id = id;
    this.dateWork = dateWork;
    this.masterId = masterId;
    this.carId = carId;
    this.serviceId = serviceId;
  }

  public Work(LocalDate dateWork, int masterId, int carId, int serviceId) {
    this.dateWork = dateWork;
    this.masterId = masterId;
    this.carId = carId;
    this.serviceId = serviceId;
  }

  public int getId() {
    return id;
  }

  public LocalDate getDateWork() {
    return dateWork;
  }

  public int getMasterId() {
    return masterId;
  }

  public int getCarId() {
    return carId;
  }

  public int getServiceId() {
    return serviceId;
  }

  public void setDateWork(LocalDate dateWork) {
    this.dateWork = dateWork;
  }

  public void setMasterId(int masterId) {
    this.masterId = masterId;
  }

  public void setCarId(int carId) {
    this.carId = carId;
  }

  public void setServiceId(int serviceId) {
    this.serviceId = serviceId;
  }
}