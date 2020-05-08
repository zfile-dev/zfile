#!/usr/bin/env bash

#=================================================
#	System Required: CentOS 6/7,Debian 8/9,Ubuntu 16+
#	Description: Z-file管理脚本
#	Version: 0.1
#	Author: wayen
#	Blog: https://www.iwayen.cn/
#=================================================

#颜色设置
Green_font_prefix="\033[32m" && Red_font_prefix="\033[31m" && Green_background_prefix="\033[42;37m" && Red_background_prefix="\033[41;37m" && Font_color_suffix="\033[0m"

#开始菜单
start_menu(){
echo && echo -e " Z-file 管理脚本 ${Red_font_prefix}[v0.1]${Font_color_suffix}
  
 ${Red_font_prefix}务必在安装z-file前先安装java环境，可直接调用脚本安装${Font_color_suffix}

 ${Green_font_prefix}0.${Font_color_suffix} 更新Z-file至最新版本
 ————————————安装————————————
 ${Green_font_prefix}1.${Font_color_suffix} 安装openjdk-8
 ${Green_font_prefix}2.${Font_color_suffix} 安装最新版本Z-file
 ${Green_font_prefix}3.${Font_color_suffix} 安装旧版Z-file(自动替换目前的zfile)
 ————————————管理————————————
 ${Green_font_prefix}4.${Font_color_suffix} 启动Z-file
 ${Green_font_prefix}5.${Font_color_suffix} 停止Z-file
 ${Green_font_prefix}6.${Font_color_suffix} 重启Z-file
 ${Green_font_prefix}7.${Font_color_suffix} 卸载Z-file
 ${Green_font_prefix}8.${Font_color_suffix} 退出脚本
————————————————————————————————" && echo

read -p " 请输入数字 [0-8]:" num
case "$num" in
	0)
	Update_zfile
	start_menu
	;;
	1)
	install_jdk
	start_menu
	;;
	2)
	install_zfile_new
	start_menu
	;;
	3)
	install_zfile_last
	start_menu
	;;
	4)
	start_zfile
	;;
	5)
	stop_zfile
	;;
	6)
	restart_zfile
	;;
	7)
	uninstall_zfile
	;;
	8)
	exit 1
	;;
	*)
	clear
	echo -e "${Error}:请输入正确数字 [0-7]"
	sleep 1s
	start_menu
	;;
esac
}

#清除之前的zfile
clear_zfile(){
	cd ~
	if [ -d "$folder"]; then
  		~/zfile/bin/stop.sh
		rm -rf ~/zfile
	fi
}

#卸载zfile
uninstall_zfile(){
	cd ~
	if [ -d "$folder"]; then
  		~/zfile/bin/stop.sh
		rm -rf ~/zfile
		rm -rf ~/.zfile*
	fi
}

#升级zfile
Update_zfile(){
	clear_zfile
	install_zfile_new
}

#安装openjdk
install_jdk(){
	if [[ "${release}" == "centos" ]]; then
		sudo yum install -y java-1.8.0-openjdk unzip
	elif [[ "${release}" == "debian" ]]; then
		sudo apt update
		sudo apt install -y openjdk-8-jre-headless unzip
	elif [[ "${release}" == "ubuntu" ]]; then
		sudo apt update
		sudo apt install -y openjdk-8-jre-headless unzip
	fi
}

#安装最新版本Z-file
install_zfile_new(){
	clear_zfile
	cd ~
	wget -P ~ https://c.jun6.net/ZFILE/zfile-release.war
	mkdir zfile && unzip zfile-release.war -d zfile && rm -rf zfile-release.war
	chmod +x ~/zfile/bin/*.sh
	echo -e "安装成功，访问地址  ${Green_font_prefix}http://ip:8080${Font_color_suffix}  
	
${Red_font_prefix}如无法访问，请检查服务器端口是否开放${Font_color_suffix}"
}

#安装Z-file
install_zfile(){
	clear_zfile
	cd ~
	wget -P ~ https://c.jun6.net/ZFILE/zfile-${1}.war
	mkdir zfile && unzip zfile-${1}.war -d zfile && rm -rf zfile-${1}.war
	chmod +x ~/zfile/bin/*.sh
	echo -e "安装成功，访问地址  ${Green_font_prefix}http://ip:8080${Font_color_suffix}  

${Red_font_prefix}如无法访问，请检查服务器端口是否开放${Font_color_suffix}"
}

#安装旧版Z-file
install_zfile_last(){
	echo && echo -e " Z-file 管理脚本 ${Red_font_prefix}[v0.1]${Font_color_suffix}

${Red_font_prefix}单盘与多盘互换时配置文件需要重新设置${Font_color_suffix}
${Red_font_prefix}但配置文件不会被删除，再次安装时可直接使用${Font_color_suffix}

${Green_font_prefix}0.${Font_color_suffix} 安装Z-file v2.2(单盘版本)
${Green_font_prefix}1.${Font_color_suffix} 安装Z-file v2.3(多盘版本)
${Green_font_prefix}2.${Font_color_suffix} 安装Z-file v2.4(多盘版本)
${Green_font_prefix}3.${Font_color_suffix} 退出脚本
————————————————————————————————" && echo

	read -p " 请输入数字 [0-3]:" num
	case "$num" in
	0)
	clear_zfile
	install_zfile 2.2
	;;
	1)
	clear_zfile
	install_zfile 2.3
	;;
	2)
	clear_zfile
	install_zfile 2.4
	;;
	3)
	exit 1
	;;
	*)
	clear
	echo -e "${Error}:请输入正确数字 [0-3]"
	sleep 1s
	install_zfile_last
	;;
	esac
}

#启动Z-file
start_zfile(){
	~/zfile/bin/start.sh
}

#停止Z-file
stop_zfile(){
	~/zfile/bin/stop.sh
}

#重启Z-file
restart_zfile(){
	~/zfile/bin/restart.sh
}

#检查系统
check_sys(){
	if [[ -f /etc/redhat-release ]]; then
		release="centos"
	elif cat /etc/issue | grep -q -E -i "debian"; then
		release="debian"
	elif cat /etc/issue | grep -q -E -i "ubuntu"; then
		release="ubuntu"
	elif cat /etc/issue | grep -q -E -i "centos|red hat|redhat"; then
		release="centos"
	elif cat /proc/version | grep -q -E -i "debian"; then
		release="debian"
	elif cat /proc/version | grep -q -E -i "ubuntu"; then
		release="ubuntu"
	elif cat /proc/version | grep -q -E -i "centos|red hat|redhat"; then
		release="centos"
    fi
}

check_sys
[[ ${release} != "debian" ]] && [[ ${release} != "ubuntu" ]] && [[ ${release} != "centos" ]] && echo -e "${Error} 本脚本不支持当前系统 ${release} !" && exit 1
start_menu
