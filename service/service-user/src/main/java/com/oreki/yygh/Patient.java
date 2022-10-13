package com.oreki.yygh;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 就诊人表
 * @TableName patient
 */
@TableName(value ="patient")
@Data
public class Patient implements Serializable {
    /**
     * 编号
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 姓名
     */
    private String name;

    /**
     * 证件类型
     */
    private String certificatesType;

    /**
     * 证件编号
     */
    private String certificatesNo;

    /**
     * 性别
     */
    private Integer sex;

    /**
     * 出生年月
     */
    private Date birthdate;

    /**
     * 手机
     */
    private String phone;

    /**
     * 是否结婚
     */
    private Integer isMarry;

    /**
     * 省code
     */
    private String provinceCode;

    /**
     * 市code
     */
    private String cityCode;

    /**
     * 区code
     */
    private String districtCode;

    /**
     * 详情地址
     */
    private String address;

    /**
     * 联系人姓名
     */
    private String contactsName;

    /**
     * 联系人证件类型
     */
    private String contactsCertificatesType;

    /**
     * 联系人证件号
     */
    private String contactsCertificatesNo;

    /**
     * 联系人手机
     */
    private String contactsPhone;

    /**
     * 就诊卡号
     */
    private String cardNo;

    /**
     * 是否有医保
     */
    private Integer isInsure;

    /**
     * 状态（0：默认 1：已认证）
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 逻辑删除(1:已删除，0:未删除)
     */
    private Integer isDeleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        Patient other = (Patient) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getName() == null ? other.getName() == null : this.getName().equals(other.getName()))
            && (this.getCertificatesType() == null ? other.getCertificatesType() == null : this.getCertificatesType().equals(other.getCertificatesType()))
            && (this.getCertificatesNo() == null ? other.getCertificatesNo() == null : this.getCertificatesNo().equals(other.getCertificatesNo()))
            && (this.getSex() == null ? other.getSex() == null : this.getSex().equals(other.getSex()))
            && (this.getBirthdate() == null ? other.getBirthdate() == null : this.getBirthdate().equals(other.getBirthdate()))
            && (this.getPhone() == null ? other.getPhone() == null : this.getPhone().equals(other.getPhone()))
            && (this.getIsMarry() == null ? other.getIsMarry() == null : this.getIsMarry().equals(other.getIsMarry()))
            && (this.getProvinceCode() == null ? other.getProvinceCode() == null : this.getProvinceCode().equals(other.getProvinceCode()))
            && (this.getCityCode() == null ? other.getCityCode() == null : this.getCityCode().equals(other.getCityCode()))
            && (this.getDistrictCode() == null ? other.getDistrictCode() == null : this.getDistrictCode().equals(other.getDistrictCode()))
            && (this.getAddress() == null ? other.getAddress() == null : this.getAddress().equals(other.getAddress()))
            && (this.getContactsName() == null ? other.getContactsName() == null : this.getContactsName().equals(other.getContactsName()))
            && (this.getContactsCertificatesType() == null ? other.getContactsCertificatesType() == null : this.getContactsCertificatesType().equals(other.getContactsCertificatesType()))
            && (this.getContactsCertificatesNo() == null ? other.getContactsCertificatesNo() == null : this.getContactsCertificatesNo().equals(other.getContactsCertificatesNo()))
            && (this.getContactsPhone() == null ? other.getContactsPhone() == null : this.getContactsPhone().equals(other.getContactsPhone()))
            && (this.getCardNo() == null ? other.getCardNo() == null : this.getCardNo().equals(other.getCardNo()))
            && (this.getIsInsure() == null ? other.getIsInsure() == null : this.getIsInsure().equals(other.getIsInsure()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()))
            && (this.getIsDeleted() == null ? other.getIsDeleted() == null : this.getIsDeleted().equals(other.getIsDeleted()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
        result = prime * result + ((getCertificatesType() == null) ? 0 : getCertificatesType().hashCode());
        result = prime * result + ((getCertificatesNo() == null) ? 0 : getCertificatesNo().hashCode());
        result = prime * result + ((getSex() == null) ? 0 : getSex().hashCode());
        result = prime * result + ((getBirthdate() == null) ? 0 : getBirthdate().hashCode());
        result = prime * result + ((getPhone() == null) ? 0 : getPhone().hashCode());
        result = prime * result + ((getIsMarry() == null) ? 0 : getIsMarry().hashCode());
        result = prime * result + ((getProvinceCode() == null) ? 0 : getProvinceCode().hashCode());
        result = prime * result + ((getCityCode() == null) ? 0 : getCityCode().hashCode());
        result = prime * result + ((getDistrictCode() == null) ? 0 : getDistrictCode().hashCode());
        result = prime * result + ((getAddress() == null) ? 0 : getAddress().hashCode());
        result = prime * result + ((getContactsName() == null) ? 0 : getContactsName().hashCode());
        result = prime * result + ((getContactsCertificatesType() == null) ? 0 : getContactsCertificatesType().hashCode());
        result = prime * result + ((getContactsCertificatesNo() == null) ? 0 : getContactsCertificatesNo().hashCode());
        result = prime * result + ((getContactsPhone() == null) ? 0 : getContactsPhone().hashCode());
        result = prime * result + ((getCardNo() == null) ? 0 : getCardNo().hashCode());
        result = prime * result + ((getIsInsure() == null) ? 0 : getIsInsure().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        result = prime * result + ((getIsDeleted() == null) ? 0 : getIsDeleted().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", userId=").append(userId);
        sb.append(", name=").append(name);
        sb.append(", certificatesType=").append(certificatesType);
        sb.append(", certificatesNo=").append(certificatesNo);
        sb.append(", sex=").append(sex);
        sb.append(", birthdate=").append(birthdate);
        sb.append(", phone=").append(phone);
        sb.append(", isMarry=").append(isMarry);
        sb.append(", provinceCode=").append(provinceCode);
        sb.append(", cityCode=").append(cityCode);
        sb.append(", districtCode=").append(districtCode);
        sb.append(", address=").append(address);
        sb.append(", contactsName=").append(contactsName);
        sb.append(", contactsCertificatesType=").append(contactsCertificatesType);
        sb.append(", contactsCertificatesNo=").append(contactsCertificatesNo);
        sb.append(", contactsPhone=").append(contactsPhone);
        sb.append(", cardNo=").append(cardNo);
        sb.append(", isInsure=").append(isInsure);
        sb.append(", status=").append(status);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", isDeleted=").append(isDeleted);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}