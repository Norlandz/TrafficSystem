module trafficPathing {
  exports com.redfrog.traffic.status;
  exports com.redfrog.traffic.service;
  exports com.redfrog.traffic.util;
  exports com.redfrog.traffic.pathing;
  exports com.redfrog.traffic.collision;
  exports com.redfrog.traffic.controller;
  exports com.redfrog.traffic.trafficLight;
  exports com.redfrog.traffic.session;
  exports com.redfrog.traffic.annotation;
  exports com.redfrog.traffic.shape;
  exports com.redfrog.traffic.exception;
  exports com.redfrog.traffic;
  exports com.redfrog.traffic.model;

  requires com.google.common;
  requires javafx.base;
  requires javafx.controls;
  requires javafx.graphics;
  requires org.apache.commons.lang3;
  requires spring.beans;
  requires spring.boot;
  requires spring.boot.autoconfigure;
  requires spring.context;
  requires spring.core;
  requires spring.web;

  opens com.redfrog.traffic;

  requires java.sql;
  requires java.desktop;
  requires lombok;
  requires javafx.swing;
}
