package framework.controller.back;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import framework.common.result.Code;
import framework.common.result.Result;
import framework.common.util.MD5.Md5Util;
import framework.common.util.oss.AliOSSUtil;

import org.springframework.stereotype.Controller;

@Controller
@RequestMapping("/back/upload")
public class uploadFileController {
	private static Logger log = LoggerFactory.getLogger(uploadFileController.class);

	@RequestMapping(value = "/UploadMultipartFile", method = RequestMethod.POST)
	@ResponseBody
	public Result uploadFile(MultipartFile mult) throws Exception {

		log.info("前端上传图片,打印图片名称:mult=" + mult.getName());

		// 上传图片返回图片上传地址
		String fileDir = System.getProperty("user.dir") + "/";
		String[] str = mult.getOriginalFilename().split("\\.", 2);
		String newFileName = new Date().getTime() + Md5Util.getSalt() + "." + str[1];
		File file = new File(fileDir + "PicFile//" + newFileName);
		try {
			FileUtils.copyInputStreamToFile(mult.getInputStream(), file);
		} catch (IOException e) {
			return Result.setData(false, Code.getMessage(Code.CNUM001));
		}
		String returnUrl = AliOSSUtil.save(file, "tbox/upload/" + file.getName(), null);
		log.info("完成图片上传,处理图片信息:图片地址:" + returnUrl);

		return Result.setData(true, returnUrl);
	}
}
