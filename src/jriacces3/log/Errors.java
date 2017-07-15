/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jriacces3.log;

/**
 *
 * @author mfernandes
 */
public enum Errors {
    ARGS_INVALIDO, //////////// 0
    ARGS_INSUFICIENTE,///////// 1
    R_VERSIOIN_ERROR,////////// 2
    THREAD_TIMEOUT,//////////// 3
    TIMEOUT_USER_INTERATION,//// 4
    FAIL_GENERAL,////////////// 5
    LOG_INICIALIZE,//////////// 6
    LOG_WRITE,///////////////// 7
    SYSTEM_ENVIRONMENT,//////// 8
    SIGNAL_STOP,/////////////// 9
    SIGNAL_EXIT,//////////////// 10
    STATUS_FAIL,///////////////// 11
    PID_INVALID,///////////////// 12
    PID_TIMEOUT;///////////////// 13
}
