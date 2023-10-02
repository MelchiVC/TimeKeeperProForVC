package com.example.opsc_poe_login

class UserData {

    data class UserData(
        val activityName: String,
        val description: String,
        val startTime: String,
        val endTime: String,
        val date: String,
        val Category: String,
        val MinGoal: String,
        val MaxGoal: String,
        val ImageUri: String
    )

//Creates a List of the users entered tasks as a companion object so it can be accessed throughout the application
    companion object {
        val dataArray: MutableList<UserData> = mutableListOf()
    }
    fun addUserData(userData: UserData) {
        dataArray.add(userData)
    }

    fun getUserDataArray(): List<UserData> {
        return dataArray
    }

}