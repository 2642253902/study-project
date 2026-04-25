import axios from "axios";

import { ElMessage } from "element-plus";

const defaultFailure = (message: string) => ElMessage.warning(message)
const defaultError = (err: any) => ElMessage.error("网络异常，请稍后再试")

export function post(
    url: string,
    data: any,
    success: (message: string, data: any) => void,
    failure: (message: string, data: any) => void = defaultFailure,
    error: (err: any) => void = defaultError
) {
    return axios.post(url, data, {
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        withCredentials: true
    }).then(response => {
        const resData = response.data;
        if (resData.success === true) {
            success(resData.message, resData.data);
        } else {
            failure(resData.message, resData.data);
        }
    }).catch(err => {
        error(err);
    });
}

export function get(
    url: string,
    success: (message: string, data: any) => void,
    failure: (message: string, data: any) => void = defaultFailure,
    error: (err: any) => void = defaultError
) {
    return axios.get(url, {

        withCredentials: true
    }).then(response => {
        const resData = response.data;
        if (resData.success) {
            success(resData.message, resData.data);
        } else {

            failure(resData.message, resData.data);
        }
    }).catch(err => {
        error(err);
    });
}
