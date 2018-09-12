package com.gomefinance.united.ucms.controller;

import com.gomefinance.framework.common.biz.pkg.page.PageableResponse;
import com.gomefinance.framework.log.api.GmfLogger;
import com.gomefinance.united.ucms.dal.model.*;
import com.gomefinance.united.ucms.facade.bean.PhotoDto;
import com.gomefinance.united.ucms.facade.bean.UserInfoManageDto;
import com.gomefinance.united.ucms.facade.bean.UserInfoUpdateDto;
import com.gomefinance.united.ucms.facade.bean.UserListDto;
import com.gomefinance.united.ucms.facade.code.UcmsRspCode;
import com.gomefinance.united.ucms.facade.common.ApiReturn;
import com.gomefinance.united.ucms.facade.enums.UserGenderEnum;
import com.gomefinance.united.ucms.facade.enums.WorkflowIdTypeEnum;
import com.gomefinance.united.ucms.facade.service.CustomerInfoManageService;
import com.gomefinance.united.ucms.service.ApprovalConfigService;
import com.gomefinance.united.ucms.service.UserInfoManageService;
import com.gomefinance.united.ucms.service.WorkflowLogService;
import com.gomefinance.united.ucs.facade.CustomerInfoUCMSFacade;
import com.gomefinance.united.ucs.facade.dto.ucms.CustomerUCMSBean;
import com.gomefinance.united.ucs.facade.dto.ucms.CustomerUCMSMainInfoBean;
import com.gomefinance.united.ucs.facade.request.ucms.GetCustomerInfoByCustomerNoRequest;
import com.gomefinance.united.ucs.facade.request.ucms.GetCustomerInfoUCMSRequest;
import com.gomefinance.united.ucs.facade.request.ucms.ModifyCustomerInfoRequest;
import com.gomefinance.united.ucs.facade.request.ucms.ModifyCustomerLoginMobileRequest;
import com.gomefinance.united.ucs.facade.response.ucms.CustomerUCMSInfoResponse;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.*;

/**
 * @Auther: liumx
 * @Date: 2018/8/14 17:12
 * @Description:
 */
@RestController
@RequestMapping("/ucms/userInfoManage")
public class UserInfoManageController {

    private static GmfLogger logger = GmfLogger.getLogger(PersonalMessageController.class);

    @Autowired
	private CustomerInfoManageService customerInfoManageService;

	@Resource
	private CustomerInfoUCMSFacade customerInfoUCMSFacade;

	/**
	 * 调用ucs接口，获取客户信息数据并分页展示
	 *
	 * @param: [userInfoManageDto]
	 * @return: com.gomefinance.united.ucms.facade.common.ApiReturn
	 * @auther: liumx
	 * @date: 2018/8/23 11:19
	 */
	@RequiresPermissions(value = {"userInfo:modifyBasicInfo","userInfo:modifyMobilePhoneNum"})
    @RequestMapping("/showUserList")
    public ApiReturn showUserList(UserInfoManageDto userInfoManageDto) {
		ApiReturn apiReturn=new ApiReturn();
		try {
			//连接客服系统接口,获取数据
			GetCustomerInfoUCMSRequest getCustomerInfoUCMSRequest=new GetCustomerInfoUCMSRequest();
			BeanUtils.copyProperties(userInfoManageDto,getCustomerInfoUCMSRequest);
			PageableResponse<CustomerUCMSBean> customerInfo = customerInfoUCMSFacade.getCustomerInfo(getCustomerInfoUCMSRequest);
			//将ucs返回的数据进行处理
			UserListDto userListDto=new UserListDto();
			List<UserInfoManageDto> userInfoManageDtos=new ArrayList<>();
			for(CustomerUCMSBean customerUCMSBean:customerInfo.getList()){
				if(customerUCMSBean.getCustomerNo()==null || "".equals(customerUCMSBean.getCustomerNo())){
					logger.info("ucs返回的空数据，不做处理");
				}else {
					UserInfoManageDto userInfo=new UserInfoManageDto();
					BeanUtils.copyProperties(customerUCMSBean,userInfo);
					userInfo.setIdTypeStr(WorkflowIdTypeEnum.valueofCode(userInfo.getIdType()));
					userInfo.setGenderStr(UserGenderEnum.valueofCode(userInfo.getGender()));
					userInfoManageDtos.add(userInfo);
				}
			}
			userListDto.setUserList(userInfoManageDtos);
			BeanUtils.copyProperties(customerInfo,userListDto);
			//将接口返回数据返回
			apiReturn.setData(userListDto);
			apiReturn.isSuccess();
		} catch (BeansException e) {
			apiReturn.setCodeMsg(UcmsRspCode.ERR_UNITED_UCMS_CUSTOMER_INFO_UPDATE);
			logger.error("展示客户信息列表错误",e);
		}
		return apiReturn;
    }


    //用户手机号查询接口
	@RequiresPermissions("userInfo:modifyMobilePhoneNum")
	@RequestMapping("/showUserPhone")
    public ApiReturn showUserPhone(@RequestParam("customerNo") String customerNo) {
		ApiReturn apiReturn=new ApiReturn();
		try {
			apiReturn=customerInfoManageService.showUserPhone(customerNo);
		} catch (Exception e) {
			apiReturn.setCodeMsg(UcmsRspCode.ERR_UNITED_UCMS_CUSTOMER_PHONE_SHOW);
			logger.error("获取客户手机号错误",e);
		}
		return apiReturn;
	}


    //用户手机号更新接口
	@RequiresPermissions("userInfo:modifyMobilePhoneNum")
	@RequestMapping("/updateUserPhone")
    public ApiReturn updateUserPhone(UserInfoUpdateDto userInfoUpdateDto) {
		ApiReturn apiReturn=new ApiReturn();
		try {
			apiReturn=customerInfoManageService.updateUserPhone(userInfoUpdateDto);
		} catch (Exception e) {
			apiReturn.setCodeMsg(UcmsRspCode.ERR_UNITED_UCMS_CUSTOMER_PHONE_UPDATE);
			logger.error("更新客户手机号错误",e);
		}
		return apiReturn;
    }

    //根据用户id查询信息
	@RequiresPermissions("userInfo:modifyBasicInfo")
	@RequestMapping("/showUserMessage")
    public ApiReturn showUserMessage(String customerNo) {
		ApiReturn apiReturn=new ApiReturn();
		try {
			apiReturn=customerInfoManageService.showUserMessage(customerNo);
		} catch (Exception e) {
			apiReturn.setCodeMsg(UcmsRspCode.ERR_UNITED_UCMS_CUSTOMER_INFO_SHOW);
			logger.error("获取客户基本信息错误",e);
		}
		return apiReturn;
    }

    //根据用户No更新信息
	@RequiresPermissions("userInfo:modifyBasicInfo")
	@RequestMapping("/updateUserMessage")
    public ApiReturn updateUserMessage(@RequestBody  UserInfoUpdateDto userInfoUpdateDto) {
		ApiReturn apiReturn=new ApiReturn();
		try {
			apiReturn=customerInfoManageService.updateUserMessage(userInfoUpdateDto);
		} catch (Exception e) {
			apiReturn.setCodeMsg(UcmsRspCode.ERR_UNITED_UCMS_CUSTOMER_INFO_UPDATE);
			logger.error("更新客户基本信息错误",e);
		}
		return apiReturn;
	}
}
