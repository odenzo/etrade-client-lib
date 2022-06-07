package com.odenzo.etrade.models
import io.circe.Codec

case class Alert(id: Long, createTime: ETimestamp, subject: String, status: AlertStatus) derives Codec.AsObject
