package framework.service.Impl;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import framework.dao.SettingRepository;
import framework.entity.po.Setting;
import framework.service.interf.SettingService;
@Service
public class SettingServiceImpl implements SettingService {
	
	@Autowired
	SettingRepository settingRepository;
	
	/**
	 * 添加或保存系统设置
	 */
	public Setting addOrUpdateSetting( Setting setting){
		Setting dbReturnSetting=null;
		if(null == setting.getId()){	
			dbReturnSetting=	settingRepository.save(setting);
		}else{
			Setting dbSetting=	settingRepository.findOne(setting.getId());
			dbSetting.setCode(setting.getCode());
			dbSetting.setTitle(setting.getTitle());
			dbSetting.setCtime(setting.getCtime());
			dbSetting.setUtime(setting.getUtime());
			dbReturnSetting=	settingRepository.save(dbSetting);
		}	
		return dbReturnSetting;
	}

}
