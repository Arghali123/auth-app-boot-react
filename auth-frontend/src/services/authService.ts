import apiClient from "@/config/apiClient";
import type RegisterData from "@/models/RegisterData";

//register function
export const registerUser=async (signupData: RegisterData)=>
{
    //api call to server to save data
    const response=await apiClient.post('/auth/register',signupData);
    return response.data;
}