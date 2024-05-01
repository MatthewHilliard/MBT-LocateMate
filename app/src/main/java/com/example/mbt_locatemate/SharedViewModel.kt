package com.example.mbt_locatemate

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

//used ChatGPT to get approach for updating friend request list
class SharedViewModel: ViewModel() {
    private val _requestList = MutableLiveData<List<Friend>>()
    val requestList: LiveData<List<Friend>> get() = _requestList

    // Function to update the friends list
    fun updateRequestList(friends: List<Friend>) {
        _requestList.value = friends
    }
}