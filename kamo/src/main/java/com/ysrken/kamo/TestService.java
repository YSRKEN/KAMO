package com.ysrken.kamo;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * �T�[�r�X�̃T���v��
 * @author ysrke
 */
@Component
public class TestService {
	@Autowired
	private LoggerService loggerService;
	
	/**
	 * �����\���p�ɕ��������������
	 * @param firstName ���O
	 * @param lastName ����
	 * @return ����������̕�����
	 */
	public String joinName(String firstName, String lastName) {
        StringBuilder builder = new StringBuilder();

        if (!StringUtils.isEmpty(firstName)) {
            builder.append(firstName);
        }

        if (!StringUtils.isEmpty(lastName)) {
            if (builder.length() > 0) {
                builder.append(" ");
            }
            builder.append(lastName);
        }

        if (builder.length() > 0) {
            String name = builder.toString();
            loggerService.debug("Saying hello to " + name);
            return "Hello " + name;
        } else {
        	loggerService.debug("Neither first name nor last name was set, saying hello to anonymous person");
            return "Hello mysterious person";
        }
	}
}
