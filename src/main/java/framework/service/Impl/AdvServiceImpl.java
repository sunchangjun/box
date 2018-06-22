package framework.service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import framework.dao.AdvRepository;
import framework.entity.po.Adv;
import framework.service.interf.AdvService;
@Service
public class AdvServiceImpl implements AdvService {
	@Autowired
	AdvRepository advRepository;
	
	public Adv saveAdv(Adv adv) {	
		return advRepository.save(adv);
	}

}
