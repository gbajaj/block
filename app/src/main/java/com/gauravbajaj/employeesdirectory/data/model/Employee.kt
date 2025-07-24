package com.gauravbajaj.employeesdirectory.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * A data class representing an employee in the application.
 *
 * @property uuid The unique identifier of the employee.
 * @property full_name The name of the employee.
 * @property email_address The email address of the employee.
 * @property photo_url_small The URL of the employee's avatar image. This can be null if the employee has no avatar.
 */
@Parcelize
data class Employee(
    val uuid: String,
    val full_name: String,
    val email_address: String,
    val photo_url_small: String? = null
) : Parcelable