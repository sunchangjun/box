package framework.entity.po;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.alibaba.fastjson.annotation.JSONField;

@Entity
@Table(name = "t_box_port_cate_rel")
public class CommodityBoxPortCateRel implements Serializable{
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	@GeneratedValue(strategy = GenerationType.AUTO)
	@Id
	@Column(name = "box_port_cate_rel_id")
	private Long boxPortCateRelId;
	
	@JSONField(serialize = false)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "box_port_cate_id" )
	private BoxPortCate boxPortCate;
	
	@Column(name="box_port_cate_id",insertable=false,updatable=false)
	private Long boxPortCateId;
	
	@JSONField(serialize = false)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "commodity_id" )
	private Commodity commodity;
	
	public Long getBoxPortCateRelId() {
		return boxPortCateRelId;
	}

	public void setBoxPortCateRelId(Long boxPortCateRelId) {
		this.boxPortCateRelId = boxPortCateRelId;
	}

	public BoxPortCate getBoxPortCate() {
		return boxPortCate;
	}

	public void setBoxPortCate(BoxPortCate boxPortCate) {
		this.boxPortCate = boxPortCate;
	}

	public Long getBoxPortCateId() {
		return boxPortCateId;
	}

	public void setBoxPortCateId(Long boxPortCateId) {
		this.boxPortCateId = boxPortCateId;
	}

	public Commodity getCommodity() {
		return commodity;
	}

	public void setCommodity(Commodity commodity) {
		this.commodity = commodity;
	}

	public Long getCommodityId() {
		return commodityId;
	}

	public void setCommodityId(Long commodityId) {
		this.commodityId = commodityId;
	}

	@Column(name="commodity_id",insertable=false,updatable=false)
	private Long commodityId;

}
