package com.bohemians.tttruck.POJO;

import com.google.gson.annotations.SerializedName

data class UserCookie(
        @SerializedName("USER_ID") val userId: Int,
        @SerializedName("PHONE") val phone: String,
        @SerializedName("PASSWORD") val password: String,
        @SerializedName("NICKNAME") val nickname: String,
        @SerializedName("NAME") val name: String?,
        @SerializedName("ACCESSTOKEN") val accessToken: String,
        @SerializedName("FCMTOKEN") val fcmToken: String?,
        @SerializedName("BUYING_SAVINGS") val buyingSavings: Int,
        @SerializedName("SELLING_SAVINGS") val sellingSavings: Int,
        @SerializedName("WASTE_SAVINGS") val wasteSavings: Int,
        @SerializedName("GREENGAS_SAVINGS") val greenGasSavings: Int,
        @SerializedName("COST_SAVINGS") val costSavings: Int,
        @SerializedName("GROUP") val group: Int,
        @SerializedName("PROFILE_IMAGE") val profileImage: String,
        @SerializedName("INTERIOR_COMPANY_TF") val interiorCompanyTF: Boolean,
        @SerializedName("INTERIOR_COMPANY_NAME") val interiorCompanyName: String?,
        @SerializedName("BIRTHDAY") val birthday: String?,
        @SerializedName("GENDER") val gender: String?,
        @SerializedName("ZIP_CODE") val zipCode: String?,
        @SerializedName("ADDRESS") val address: String?,
        @SerializedName("DETAIL_ADDRESS") val detailAddress: String?,
        @SerializedName("JOIN_STATE") val joinState: String?,
        @SerializedName("RESTING_TF") val restingTF: Boolean,
        @SerializedName("LEAVE_TF") val leaveTF: Boolean,
        @SerializedName("PHONE_AUTH_CODE") val phoneAuthCode: String,
        @SerializedName("PHONE_AUTH_DATE") val phoneAuthDate: String?,
        @SerializedName("PHONE_AUTH_SUCCEED_DATE") val phoneAuthSucceedDate: String?,
        @SerializedName("PHONE_AUTH_TF") val phoneAuthTF: Boolean,
        @SerializedName("REG_TIME") val regTime: String,
        @SerializedName("UPD_TIME") val updTime: String,
        @SerializedName("JOIN_TIME") val joinTime: String,
        @SerializedName("JOIN_PERMIT_USER_ID") val joinPermitUserId: String?,
        @SerializedName("JOIN_AGREE") val joinAgree: String,
        @SerializedName("AGREE_UPD_TIME") val agreeUpdTime: String,
        @SerializedName("ACCESS_TIME") val accessTime: String,
        @SerializedName("tt_user_talkplu") val ttUserTalkPlu: TtUserTalkPlu
)

data class TtUserTalkPlu(
        @SerializedName("USER_ID") val userId: Int,
        @SerializedName("TALKPLUS_ID") val talkPlusId: String,
        @SerializedName("TALKPLUS_PASSWORD") val talkPlusPassword: String,
        @SerializedName("TALKPLUS_USERNAME") val talkPlusUsername: String,
        @SerializedName("TALKPLUS_PROFILE_IMAGE_URL") val talkPlusProfileImageUrl: String,
        @SerializedName("TALKPLUS_LOGIN_TOKEN") val talkPlusLoginToken: String,
        @SerializedName("LEAVE_TF") val leaveTF: Boolean
)
