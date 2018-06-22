package framework.service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import framework.dao.auth.AuthorityRepository;
import framework.entity.po.auth.Authority;
import framework.service.AuthorityService;
@Service
public class AuthorityServiceImpl implements AuthorityService {
	
	
	@Autowired
	AuthorityRepository authorityRepository;
	
	
	public  Authority saveAuthority(Authority authority){	
		return  authorityRepository.save(authority);
	}

}
