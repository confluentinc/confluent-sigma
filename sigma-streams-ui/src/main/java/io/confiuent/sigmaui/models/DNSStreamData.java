package io.confiuent.sigmaui.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class DNSStreamData {
    private Double ts;

    private String uid;

    @JsonProperty("id.orig_h")
    private String id_orig_h;

    @JsonProperty("id.orig_p")
    private String id_orig_p;

    @JsonProperty("id.resp_h")
    private String id_resp_h;

    @JsonProperty("id.resp_p")
    private String id_resp_p;

    private String proto;

    private Integer trans_id;

    private String query;

    private Integer qclass;

    private String qclass_name;

    private Integer qtype;

    private String qtype_name;

    private Integer rcode;

    private String rcode_name;

    private Boolean AA;

    private Boolean TC;

    private Boolean RD;

    private Boolean TA;

    private Integer Z;

    private List<String> answers;

    private List<Double> TTLs;

    private Boolean rejected;

    public Double getTs() {
        return this.ts;
    }

    public void setTs(Double ts) {
        this.ts = ts;
    }

    public String getUid() {
        return this.uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getId_orig_h() {
        return this.id_orig_h;
    }

    public void setId_orig_h(String id_orig_h) {
        this.id_orig_h = id_orig_h;
    }

    public String getId_orig_p() {
        return this.id_orig_p;
    }

    public void setId_orig_p(String id_orig_p) {
        this.id_orig_p = id_orig_p;
    }

    public String getId_resp_h() {
        return this.id_resp_h;
    }

    public void setId_resp_h(String id_resp_h) {
        this.id_resp_h = id_resp_h;
    }

    public String getId_resp_p() {
        return this.id_resp_p;
    }

    public void setId_resp_p(String id_resp_p) {
        this.id_resp_p = id_resp_p;
    }

    public String getProto() {
        return this.proto;
    }

    public void setProto(String proto) {
        this.proto = proto;
    }

    public Integer getTrans_id() {
        return this.trans_id;
    }

    public void setTrans_id(Integer trans_id) {
        this.trans_id = trans_id;
    }

    public String getQuery() {
        return this.query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Integer getQclass() {
        return this.qclass;
    }

    public void setQclass(Integer qclass) {
        this.qclass = qclass;
    }

    public String getQclass_name() {
        return this.qclass_name;
    }

    public void setQclass_name(String qclass_name) {
        this.qclass_name = qclass_name;
    }

    public Integer getQtype() {
        return this.qtype;
    }

    public void setQtype(Integer qtype) {
        this.qtype = qtype;
    }

    public String getQtype_name() {
        return this.qtype_name;
    }

    public void setQtype_name(String qtype_name) {
        this.qtype_name = qtype_name;
    }

    public Integer getRcode() {
        return this.rcode;
    }

    public void setRcode(Integer rcode) {
        this.rcode = rcode;
    }

    public String getRcode_name() {
        return this.rcode_name;
    }

    public void setRcode_name(String rcode_name) {
        this.rcode_name = rcode_name;
    }

    public Boolean getAA() {
        return this.AA;
    }

    public void setAA(Boolean AA) {
        this.AA = AA;
    }

    public Boolean getTC() {
        return this.TC;
    }

    public void setTC(Boolean TC) {
        this.TC = TC;
    }

    public Boolean getRD() {
        return this.RD;
    }

    public void setRD(Boolean RD) {
        this.RD = RD;
    }

    public Boolean getTA() {
        return this.TA;
    }

    public void setTA(Boolean TA) {
        this.TA = TA;
    }

    public Integer getZ() {
        return this.Z;
    }

    public void setZ(Integer z) {
        this.Z = z;
    }

    public List<String> getAnswers() {
        return this.answers;
    }

    public void setAnswers(List<String> answers) {
        this.answers = answers;
    }

    public List<Double> getTTLs() {
        return this.TTLs;
    }

    public void setTTLs(List<Double> TTLs) {
        this.TTLs = TTLs;
    }

    public Boolean getRejected() {
        return this.rejected;
    }

    public void setRejected(Boolean rejected) {
        this.rejected = rejected;
    }

    public String toString() {
        return "DNSStreamData{ts=" + this.ts + ", uid='" + this.uid + "', id_orig_h='" + this.id_orig_h + "', id_orig_p='" + this.id_orig_p + "', id_resp_h='" + this.id_resp_h + "', id_resp_p='" + this.id_resp_p + "', proto='" + this.proto + "', trans_id=" + this.trans_id + ", query='" + this.query + "', qclass=" + this.qclass + ", qclass_name='" + this.qclass_name + "', qtype=" + this.qtype + ", qtype_name='" + this.qtype_name + "', rcode=" + this.rcode + ", rcode_name='" + this.rcode_name + "', AA=" + this.AA + ", TC=" + this.TC + ", RD=" + this.RD + ", TA=" + this.TA + ", Z=" + this.Z + ", answers=" + this.answers + ", TTLs=" + this.TTLs + ", rejected=" + this.rejected + "}";
    }
}
