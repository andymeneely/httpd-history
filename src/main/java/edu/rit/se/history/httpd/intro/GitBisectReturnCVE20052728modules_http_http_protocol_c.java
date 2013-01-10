package edu.rit.se.history.httpd.intro;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

/**
 * CVE-20052728
 * Vulnerable file: modules/http/http_protocol.c
 * Fix commit: //___FIX___
 * 
 * <pre>
 *  ./tryBisect.sh 20052728 modules/http/http_protocol.c //___FIX___ GitBisectReturnCVE20052728modules_http_http_protocol_c
 * </pre>
 *
 * Result: _ is the first bad commit
 *
 * @author Alberto Rodriguez
 * 
 */
public class GitBisectReturnCVE20052728modules_http_http_protocol_c {

	private static final int GOOD_RETURN_CODE = 0;
	private static final int BAD_RETURN_CODE = 1;
	private static final int SKIP_RETURN_CODE = 125;

	// Context from vulnerable version.
	private static List<String> oldBlocks;

	// Context from fixed version.
	private static List<String> newBlocks;
    
    private static final String CVE = "CVE-20052728";
	private static final String FILE = "modules/http/http_protocol.c";

	public static void main(String[] args) {
		if (args.length > 0) {
			System.out.println("No arguments required to this script!");
		}

		        newBlocks = Arrays.asList(
            "returnNULL;}",
            "returnstatus_lines[ap_index_of_response(status)];}",
            "returnlist;}AP_DECLARE(int)ap_send_http_options(request_rec*r){if(r->assbackwards){",
            "returnOK;}AP_DECLARE(void)ap_set_content_type(request_rec*r,constchar*ct){if(!ct){",
            "}}staticconstchar*add_optional_notes(request_rec*r,constchar*prefix,constchar*key,",
            "l->method_list->nelts=0;}");

        oldBlocks = Arrays.asList(
            "returnNULL;}staticlongget_chunk_size(char*);typedefstructhttp_filter_ctx{apr_off_tremaining;apr_off_tlimit;apr_off_tlimit_used;enum{BODY_NONE,BODY_LENGTH,BODY_CHUNK}state;inteos_sent;}http_ctx_t;apr_status_tap_http_filter(ap_filter_t*f,apr_bucket_brigade*b,ap_input_mode_tmode,apr_read_type_eblock,apr_off_treadbytes){apr_bucket*e;http_ctx_t*ctx=f->ctx;apr_status_trv;apr_off_ttotalread;if(mode!=AP_MODE_READBYTES&&mode!=AP_MODE_GETLINE){returnap_get_brigade(f->next,b,mode,block,readbytes);}if(!ctx){constchar*tenc,*lenp;f->ctx=ctx=apr_palloc(f->r->pool,sizeof(*ctx));ctx->state=BODY_NONE;ctx->remaining=0;ctx->limit_used=0;ctx->eos_sent=0;if(!f->r->proxyreq){ctx->limit=ap_get_limit_req_body(f->r);}else{ctx->limit=0;}tenc=apr_table_get(f->r->headers_in,Transfer-Encoding);lenp=apr_table_get(f->r->headers_in,Content-Length);if(tenc){if(!strcasecmp(tenc,chunked)){ctx->state=BODY_CHUNK;}}elseif(lenp){char*endstr;ctx->state=BODY_LENGTH;errno=0;if(apr_strtoff(&ctx->remaining,lenp,&endstr,10)||endstr==lenp||*endstr||ctx->remaining<0){apr_bucket_brigade*bb;ctx->remaining=0;ap_log_rerror(APLOG_MARK,APLOG_ERR,0,f->r,InvalidContent-Length);bb=apr_brigade_create(f->r->pool,f->c->bucket_alloc);e=ap_bucket_error_create(HTTP_REQUEST_ENTITY_TOO_LARGE,NULL,f->r->pool,f->c->bucket_alloc);APR_BRIGADE_INSERT_TAIL(bb,e);e=apr_bucket_eos_create(f->c->bucket_alloc);APR_BRIGADE_INSERT_TAIL(bb,e);ctx->eos_sent=1;returnap_pass_brigade(f->r->output_filters,bb);}if(ctx->limit&&ctx->limit<ctx->remaining){apr_bucket_brigade*bb;ap_log_rerror(APLOG_MARK,APLOG_ERR,0,f->r,Requestedcontent-lengthof%APR_OFF_T_FMTislargerthantheconfiguredlimitof%APR_OFF_T_FMT,ctx->remaining,ctx->limit);bb=apr_brigade_create(f->r->pool,f->c->bucket_alloc);e=ap_bucket_error_create(HTTP_REQUEST_ENTITY_TOO_LARGE,NULL,f->r->pool,f->c->bucket_alloc);APR_BRIGADE_INSERT_TAIL(bb,e);e=apr_bucket_eos_create(f->c->bucket_alloc);APR_BRIGADE_INSERT_TAIL(bb,e);ctx->eos_sent=1;returnap_pass_brigade(f->r->output_filters,bb);}}if(ctx->state==BODY_NONE&&f->r->proxyreq!=PROXYREQ_RESPONSE){e=apr_bucket_eos_create(f->c->bucket_alloc);APR_BRIGADE_INSERT_TAIL(b,e);ctx->eos_sent=1;returnAPR_SUCCESS;}if((ctx->state==BODY_CHUNK||(ctx->state==BODY_LENGTH&&ctx->remaining>0))&&f->r->expecting_100&&f->r->proto_num>=HTTP_VERSION(1,1)){char*tmp;apr_bucket_brigade*bb;tmp=apr_pstrcat(f->r->pool,AP_SERVER_PROTOCOL,,status_lines[0],CRLFCRLF,NULL);bb=apr_brigade_create(f->r->pool,f->c->bucket_alloc);e=apr_bucket_pool_create(tmp,strlen(tmp),f->r->pool,f->c->bucket_alloc);APR_BRIGADE_INSERT_HEAD(bb,e);e=apr_bucket_flush_create(f->c->bucket_alloc);APR_BRIGADE_INSERT_TAIL(bb,e);ap_pass_brigade(f->c->output_filters,bb);}if(ctx->state==BODY_CHUNK){charline[30];apr_bucket_brigade*bb;apr_size_tlen=30;apr_off_tbrigade_length;bb=apr_brigade_create(f->r->pool,f->c->bucket_alloc);rv=ap_get_brigade(f->next,bb,AP_MODE_GETLINE,APR_BLOCK_READ,0);if(rv==APR_SUCCESS){rv=apr_brigade_length(bb,1,&brigade_length);if(rv==APR_SUCCESS&&brigade_length>f->r->server->limit_req_line){rv=APR_ENOSPC;}if(rv==APR_SUCCESS){rv=apr_brigade_flatten(bb,line,&len);if(rv==APR_SUCCESS){ctx->remaining=get_chunk_size(line);}}}apr_brigade_cleanup(bb);if(rv!=APR_SUCCESS||ctx->remaining<0){ctx->remaining=0;e=ap_bucket_error_create(HTTP_REQUEST_ENTITY_TOO_LARGE,NULL,f->r->pool,f->c->bucket_alloc);APR_BRIGADE_INSERT_TAIL(bb,e);e=apr_bucket_eos_create(f->c->bucket_alloc);APR_BRIGADE_INSERT_TAIL(bb,e);ctx->eos_sent=1;returnap_pass_brigade(f->r->output_filters,bb);}if(!ctx->remaining){ctx->state=BODY_NONE;ap_get_mime_headers(f->r);e=apr_bucket_eos_create(f->c->bucket_alloc);APR_BRIGADE_INSERT_TAIL(b,e);ctx->eos_sent=1;returnAPR_SUCCESS;}}}if(ctx->eos_sent){e=apr_bucket_eos_create(f->c->bucket_alloc);APR_BRIGADE_INSERT_TAIL(b,e);returnAPR_SUCCESS;}if(!ctx->remaining){switch(ctx->state){caseBODY_NONE:break;caseBODY_LENGTH:e=apr_bucket_eos_create(f->c->bucket_alloc);APR_BRIGADE_INSERT_TAIL(b,e);ctx->eos_sent=1;returnAPR_SUCCESS;caseBODY_CHUNK:{charline[30];apr_bucket_brigade*bb;apr_size_tlen=30;bb=apr_brigade_create(f->r->pool,f->c->bucket_alloc);rv=ap_get_brigade(f->next,bb,AP_MODE_GETLINE,APR_BLOCK_READ,0);apr_brigade_cleanup(bb);if(rv==APR_SUCCESS){rv=ap_get_brigade(f->next,bb,AP_MODE_GETLINE,APR_BLOCK_READ,0);if(rv==APR_SUCCESS){rv=apr_brigade_flatten(bb,line,&len);if(rv==APR_SUCCESS){ctx->remaining=get_chunk_size(line);}}apr_brigade_cleanup(bb);}if(rv!=APR_SUCCESS||ctx->remaining<0){ctx->remaining=0;e=ap_bucket_error_create(HTTP_REQUEST_ENTITY_TOO_LARGE,NULL,f->r->pool,f->c->bucket_alloc);APR_BRIGADE_INSERT_TAIL(bb,e);e=apr_bucket_eos_create(f->c->bucket_alloc);APR_BRIGADE_INSERT_TAIL(bb,e);ctx->eos_sent=1;returnap_pass_brigade(f->r->output_filters,bb);}if(!ctx->remaining){ctx->state=BODY_NONE;ap_get_mime_headers(f->r);e=apr_bucket_eos_create(f->c->bucket_alloc);APR_BRIGADE_INSERT_TAIL(b,e);ctx->eos_sent=1;returnAPR_SUCCESS;}}break;}}if(ctx->state==BODY_LENGTH||ctx->state==BODY_CHUNK){if(ctx->remaining<readbytes){readbytes=ctx->remaining;}AP_DEBUG_ASSERT(readbytes>0);}rv=ap_get_brigade(f->next,b,mode,block,readbytes);if(rv!=APR_SUCCESS){returnrv;}apr_brigade_length(b,0,&totalread);AP_DEBUG_ASSERT(totalread>=0);if(ctx->state!=BODY_NONE){ctx->remaining-=totalread;}if(ctx->state==BODY_LENGTH&&ctx->remaining==0){e=apr_bucket_eos_create(f->c->bucket_alloc);APR_BRIGADE_INSERT_TAIL(b,e);}if(ctx->limit){ctx->limit_used+=totalread;if(ctx->limit<ctx->limit_used){apr_bucket_brigade*bb;ap_log_rerror(APLOG_MARK,APLOG_ERR,0,f->r,Readcontent-lengthof%APR_OFF_T_FMTislargerthantheconfiguredlimitof%APR_OFF_T_FMT,ctx->limit_used,ctx->limit);bb=apr_brigade_create(f->r->pool,f->c->bucket_alloc);e=ap_bucket_error_create(HTTP_REQUEST_ENTITY_TOO_LARGE,NULL,f->r->pool,f->c->bucket_alloc);APR_BRIGADE_INSERT_TAIL(bb,e);e=apr_bucket_eos_create(f->c->bucket_alloc);APR_BRIGADE_INSERT_TAIL(bb,e);ctx->eos_sent=1;returnap_pass_brigade(f->r->output_filters,bb);}}returnAPR_SUCCESS;}",
            "returnstatus_lines[ap_index_of_response(status)];}typedefstructheader_struct{apr_pool_t*pool;apr_bucket_brigade*bb;}header_struct;staticintform_header_field(header_struct*h,constchar*fieldname,constchar*fieldval){#ifAPR_CHARSET_EBCDICchar*headfield;apr_size_tlen;apr_size_tname_len;apr_size_tval_len;char*next;name_len=strlen(fieldname);val_len=strlen(fieldval);len=name_len+val_len+4;headfield=(char*)apr_palloc(h->pool,len+1);memcpy(headfield,fieldname,name_len);next=headfield+name_len;*next++=':';*next++='';memcpy(next,fieldval,val_len);next+=val_len;*next++=CR;*next++=LF;*next=0;ap_xlate_proto_to_ascii(headfield,len);apr_brigade_write(h->bb,NULL,NULL,headfield,len);#elsestructiovecvec[4];structiovec*v=vec;v->iov_base=(void*)fieldname;v->iov_len=strlen(fieldname);v++;v->iov_base=:;v->iov_len=sizeof(:)-1;v++;v->iov_base=(void*)fieldval;v->iov_len=strlen(fieldval);v++;v->iov_base=CRLF;v->iov_len=sizeof(CRLF)-1;apr_brigade_writev(h->bb,NULL,NULL,vec,4);#endifreturn1;}staticapr_status_tsend_all_header_fields(header_struct*h,constrequest_rec*r){constapr_array_header_t*elts;constapr_table_entry_t*t_elt;constapr_table_entry_t*t_end;structiovec*vec;structiovec*vec_next;elts=apr_table_elts(r->headers_out);if(elts->nelts==0){returnAPR_SUCCESS;}t_elt=(constapr_table_entry_t*)(elts->elts);t_end=t_elt+elts->nelts;vec=(structiovec*)apr_palloc(h->pool,4*elts->nelts*sizeof(structiovec));vec_next=vec;do{vec_next->iov_base=(void*)(t_elt->key);vec_next->iov_len=strlen(t_elt->key);vec_next++;vec_next->iov_base=:;vec_next->iov_len=sizeof(:)-1;vec_next++;vec_next->iov_base=(void*)(t_elt->val);vec_next->iov_len=strlen(t_elt->val);vec_next++;vec_next->iov_base=CRLF;vec_next->iov_len=sizeof(CRLF)-1;vec_next++;t_elt++;}while(t_elt<t_end);#ifAPR_CHARSET_EBCDIC{apr_size_tlen;char*tmp=apr_pstrcatv(r->pool,vec,vec_next-vec,&len);ap_xlate_proto_to_ascii(tmp,len);returnapr_brigade_write(h->bb,NULL,NULL,tmp,len);}#elsereturnapr_brigade_writev(h->bb,NULL,NULL,vec,vec_next-vec);#endif}staticvoidbasic_http_header_check(request_rec*r,constchar**protocol){if(r->assbackwards){return;}if(!r->status_line){r->status_line=status_lines[ap_index_of_response(r->status)];}if(r->proto_num>HTTP_VERSION(1,0)&&apr_table_get(r->subprocess_env,downgrade-1.0)){r->proto_num=HTTP_VERSION(1,0);}if(r->proto_num==HTTP_VERSION(1,0)&&apr_table_get(r->subprocess_env,force-response-1.0)){*protocol=HTTP/1.0;r->connection->keepalive=AP_CONN_CLOSE;}else{*protocol=AP_SERVER_PROTOCOL;}}staticvoidbasic_http_header(request_rec*r,apr_bucket_brigade*bb,constchar*protocol){char*date;constchar*server;header_structh;structiovecvec[4];if(r->assbackwards){return;}vec[0].iov_base=(void*)protocol;vec[0].iov_len=strlen(protocol);vec[1].iov_base=(void*);vec[1].iov_len=sizeof()-1;vec[2].iov_base=(void*)(r->status_line);vec[2].iov_len=strlen(r->status_line);vec[3].iov_base=(void*)CRLF;vec[3].iov_len=sizeof(CRLF)-1;#ifAPR_CHARSET_EBCDIC{char*tmp;apr_size_tlen;tmp=apr_pstrcatv(r->pool,vec,4,&len);ap_xlate_proto_to_ascii(tmp,len);apr_brigade_write(bb,NULL,NULL,tmp,len);}#elseapr_brigade_writev(bb,NULL,NULL,vec,4);#endifdate=apr_palloc(r->pool,APR_RFC822_DATE_LEN);ap_recent_rfc822_date(date,r->request_time);h.pool=r->pool;h.bb=bb;form_header_field(&h,Date,date);if(r->proxyreq!=PROXYREQ_NONE){server=apr_table_get(r->headers_out,Server);if(server){form_header_field(&h,Server,server);}}else{form_header_field(&h,Server,ap_get_server_version());}apr_table_unset(r->headers_out,Date);apr_table_unset(r->headers_out,Server);}AP_DECLARE(void)ap_basic_http_header(request_rec*r,apr_bucket_brigade*bb){constchar*protocol;basic_http_header_check(r,&protocol);basic_http_header(r,bb,protocol);}staticvoidterminate_header(apr_bucket_brigade*bb){chartmp[]=X-Pad:avoidbrowserbugCRLF;charcrlf[]=CRLF;apr_off_tlen;apr_size_tbuflen;(void)apr_brigade_length(bb,1,&len);if(len>=255&&len<=257){buflen=strlen(tmp);ap_xlate_proto_to_ascii(tmp,buflen);apr_brigade_write(bb,NULL,NULL,tmp,buflen);}buflen=strlen(crlf);ap_xlate_proto_to_ascii(crlf,buflen);apr_brigade_write(bb,NULL,NULL,crlf,buflen);}",
            "returnlist;}AP_DECLARE_NONSTD(int)ap_send_http_trace(request_rec*r){intrv;apr_bucket_brigade*b;header_structh;if(r->method_number!=M_TRACE){returnDECLINED;}while(r->prev){r=r->prev;}if((rv=ap_setup_client_block(r,REQUEST_NO_BODY))){returnrv;}ap_set_content_type(r,message/http);b=apr_brigade_create(r->pool,r->connection->bucket_alloc);apr_brigade_putstrs(b,NULL,NULL,r->the_request,CRLF,NULL);h.pool=r->pool;h.bb=b;apr_table_do((int(*)(void*,constchar*,constchar*))form_header_field,(void*)&h,r->headers_in,NULL);apr_brigade_puts(b,NULL,NULL,CRLF);ap_pass_brigade(r->output_filters,b);returnDONE;}AP_DECLARE(int)ap_send_http_options(request_rec*r){if(r->assbackwards){",
            "returnOK;}staticintuniq_field_values(void*d,constchar*key,constchar*val){apr_array_header_t*values;char*start;char*e;char**strpp;inti;values=(apr_array_header_t*)d;e=apr_pstrdup(values->pool,val);do{while(*e==','||apr_isspace(*e)){++e;}if(*e=='0'){break;}start=e;while(*e!='0'&&*e!=','&&!apr_isspace(*e)){++e;}if(*e!='0'){*e++='0';}for(i=0,strpp=(char**)values->elts;i<values->nelts;++i,++strpp){if(*strpp&&strcasecmp(*strpp,start)==0){break;}}if(i==values->nelts){*(char**)apr_array_push(values)=start;}}while(*e!='0');return1;}staticvoidfixup_vary(request_rec*r){apr_array_header_t*varies;varies=apr_array_make(r->pool,5,sizeof(char*));apr_table_do((int(*)(void*,constchar*,constchar*))uniq_field_values,(void*)varies,r->headers_out,Vary,NULL);if(varies->nelts>0){apr_table_setn(r->headers_out,Vary,apr_array_pstrcat(r->pool,varies,','));}}AP_DECLARE(void)ap_set_content_type(request_rec*r,constchar*ct){if(!ct){",
            "}}typedefstructheader_filter_ctx{intheaders_sent;}header_filter_ctx;AP_CORE_DECLARE_NONSTD(apr_status_t)ap_http_header_filter(ap_filter_t*f,apr_bucket_brigade*b){request_rec*r=f->r;conn_rec*c=r->connection;constchar*clheader;constchar*protocol;apr_bucket*e;apr_bucket_brigade*b2;header_structh;header_filter_ctx*ctx=f->ctx;AP_DEBUG_ASSERT(!r->main);if(r->header_only){if(!ctx){ctx=f->ctx=apr_pcalloc(r->pool,sizeof(header_filter_ctx));}elseif(ctx->headers_sent){apr_brigade_destroy(b);returnOK;}}for(e=APR_BRIGADE_FIRST(b);e!=APR_BRIGADE_SENTINEL(b);e=APR_BUCKET_NEXT(e)){if(e->type==&ap_bucket_type_error){ap_bucket_error*eb=e->data;ap_die(eb->status,r);returnAP_FILTER_ERROR;}}if(r->assbackwards){r->sent_bodyct=1;ap_remove_output_filter(f);returnap_pass_brigade(f->next,b);}if(!apr_is_empty_table(r->err_headers_out)){r->headers_out=apr_table_overlay(r->pool,r->err_headers_out,r->headers_out);}if(apr_table_get(r->subprocess_env,force-no-vary)!=NULL){apr_table_unset(r->headers_out,Vary);r->proto_num=HTTP_VERSION(1,0);apr_table_set(r->subprocess_env,force-response-1.0,1);}else{fixup_vary(r);}if(apr_table_get(r->notes,no-etag)!=NULL){apr_table_unset(r->headers_out,ETag);}basic_http_header_check(r,&protocol);ap_set_keepalive(r);if(r->chunked){apr_table_mergen(r->headers_out,Transfer-Encoding,chunked);apr_table_unset(r->headers_out,Content-Length);}apr_table_setn(r->headers_out,Content-Type,ap_make_content_type(r,r->content_type));if(r->content_encoding){apr_table_setn(r->headers_out,Content-Encoding,r->content_encoding);}if(!apr_is_empty_array(r->content_languages)){inti;char**languages=(char**)(r->content_languages->elts);for(i=0;i<r->content_languages->nelts;++i){apr_table_mergen(r->headers_out,Content-Language,languages[i]);}}if(r->no_cache&&!apr_table_get(r->headers_out,Expires)){char*date=apr_palloc(r->pool,APR_RFC822_DATE_LEN);ap_recent_rfc822_date(date,r->request_time);apr_table_addn(r->headers_out,Expires,date);}if(r->header_only&&(clheader=apr_table_get(r->headers_out,Content-Length))&&!strcmp(clheader,0)){apr_table_unset(r->headers_out,Content-Length);}b2=apr_brigade_create(r->pool,c->bucket_alloc);basic_http_header(r,b2,protocol);h.pool=r->pool;h.bb=b2;if(r->status==HTTP_NOT_MODIFIED){apr_table_do((int(*)(void*,constchar*,constchar*))form_header_field,(void*)&h,r->headers_out,Connection,Keep-Alive,ETag,Content-Location,Expires,Cache-Control,Vary,Warning,WWW-Authenticate,Proxy-Authenticate,Set-Cookie,Set-Cookie2,NULL);}else{send_all_header_fields(&h,r);}terminate_header(b2);ap_pass_brigade(f->next,b2);if(r->header_only){apr_brigade_destroy(b);ctx->headers_sent=1;returnOK;}r->sent_bodyct=1;if(r->chunked){ap_add_output_filter(CHUNK,NULL,r,r->connection);}ap_remove_output_filter(f);returnap_pass_brigade(f->next,b);}AP_DECLARE(int)ap_setup_client_block(request_rec*r,intread_policy){constchar*tenc=apr_table_get(r->headers_in,Transfer-Encoding);constchar*lenp=apr_table_get(r->headers_in,Content-Length);r->read_body=read_policy;r->read_chunked=0;r->remaining=0;if(tenc){if(strcasecmp(tenc,chunked)){ap_log_rerror(APLOG_MARK,APLOG_ERR,0,r,UnknownTransfer-Encoding%s,tenc);returnHTTP_NOT_IMPLEMENTED;}if(r->read_body==REQUEST_CHUNKED_ERROR){ap_log_rerror(APLOG_MARK,APLOG_ERR,0,r,chunkedTransfer-Encodingforbidden:%s,r->uri);return(lenp)?HTTP_BAD_REQUEST:HTTP_LENGTH_REQUIRED;}r->read_chunked=1;}elseif(lenp){char*endstr;if(apr_strtoff(&r->remaining,lenp,&endstr,10)||*endstr||r->remaining<0){r->remaining=0;ap_log_rerror(APLOG_MARK,APLOG_ERR,0,r,InvalidContent-Length);returnHTTP_BAD_REQUEST;}}if((r->read_body==REQUEST_NO_BODY)&&(r->read_chunked||(r->remaining>0))){ap_log_rerror(APLOG_MARK,APLOG_ERR,0,r,%swithbodyisnotallowedfor%s,r->method,r->uri);returnHTTP_REQUEST_ENTITY_TOO_LARGE;}#ifdefAP_DEBUG{core_request_config*req_cfg=(core_request_config*)ap_get_module_config(r->request_config,&core_module);AP_DEBUG_ASSERT(APR_BRIGADE_EMPTY(req_cfg->bb));}#endifreturnOK;}AP_DECLARE(int)ap_should_client_block(request_rec*r){if(r->read_length||(!r->read_chunked&&(r->remaining<=0))){return0;}return1;}staticlongget_chunk_size(char*b){longchunksize=0;size_tchunkbits=sizeof(long)*8;while(*b=='0'){++b;}while(apr_isxdigit(*b)&&(chunkbits>0)){intxvalue=0;if(*b>='0'&&*b<='9'){xvalue=*b-'0';}elseif(*b>='A'&&*b<='F'){xvalue=*b-'A'+0xa;}elseif(*b>='a'&&*b<='f'){xvalue=*b-'a'+0xa;}chunksize=(chunksize<<4)|xvalue;chunkbits-=4;++b;}if(apr_isxdigit(*b)&&(chunkbits<=0)){return-1;}returnchunksize;}AP_DECLARE(long)ap_get_client_block(request_rec*r,char*buffer,apr_size_tbufsiz){apr_status_trv;apr_bucket_brigade*bb;if(r->remaining<0||(!r->read_chunked&&r->remaining==0)){return0;}bb=apr_brigade_create(r->pool,r->connection->bucket_alloc);if(bb==NULL){r->connection->keepalive=AP_CONN_CLOSE;return-1;}rv=ap_get_brigade(r->input_filters,bb,AP_MODE_READBYTES,APR_BLOCK_READ,bufsiz);if(rv!=APR_SUCCESS){r->connection->keepalive=AP_CONN_CLOSE;apr_brigade_destroy(bb);return-1;}AP_DEBUG_ASSERT(!APR_BRIGADE_EMPTY(bb));if(APR_BUCKET_IS_EOS(APR_BRIGADE_LAST(bb))){if(r->read_chunked){r->remaining=-1;}else{r->remaining=0;}}rv=apr_brigade_flatten(bb,buffer,&bufsiz);if(rv!=APR_SUCCESS){apr_brigade_destroy(bb);return-1;}r->read_length+=bufsiz;apr_brigade_destroy(bb);returnbufsiz;}AP_DECLARE(int)ap_discard_request_body(request_rec*r){apr_bucket_brigade*bb;intrv,seen_eos;if(r->main||r->connection->keepalive==AP_CONN_CLOSE||ap_status_drops_connection(r->status)){returnOK;}bb=apr_brigade_create(r->pool,r->connection->bucket_alloc);seen_eos=0;do{apr_bucket*bucket;rv=ap_get_brigade(r->input_filters,bb,AP_MODE_READBYTES,APR_BLOCK_READ,HUGE_STRING_LEN);if(rv!=APR_SUCCESS){if(rv==AP_FILTER_ERROR){apr_brigade_destroy(bb);returnrv;}else{apr_brigade_destroy(bb);returnHTTP_BAD_REQUEST;}}for(bucket=APR_BRIGADE_FIRST(bb);bucket!=APR_BRIGADE_SENTINEL(bb);bucket=APR_BUCKET_NEXT(bucket)){constchar*data;apr_size_tlen;if(APR_BUCKET_IS_EOS(bucket)){seen_eos=1;break;}if(bucket->length==0){continue;}rv=apr_bucket_read(bucket,&data,&len,APR_BLOCK_READ);if(rv!=APR_SUCCESS){apr_brigade_destroy(bb);returnHTTP_BAD_REQUEST;}}apr_brigade_cleanup(bb);}while(!seen_eos);returnOK;}staticconstchar*add_optional_notes(request_rec*r,constchar*prefix,constchar*key,",
            "l->method_list->nelts=0;}#defineHEX_DIGITS0123456789abcdefstaticchar*etag_ulong_to_hex(char*next,unsignedlongu){intprinting=0;intshift=sizeof(unsignedlong)*8-4;do{unsignedlongnext_digit=((u>>shift)&(unsignedlong)0xf);if(next_digit){*next++=HEX_DIGITS[next_digit];printing=1;}elseif(printing){*next++=HEX_DIGITS[next_digit];}shift-=4;}while(shift);*next++=HEX_DIGITS[u&(unsignedlong)0xf];returnnext;}#defineETAG_WEAKW/#defineCHARS_PER_UNSIGNED_LONG(sizeof(unsignedlong)*2)AP_DECLARE(char*)ap_make_etag(request_rec*r,intforce_weak){char*weak;apr_size_tweak_len;char*etag;char*next;core_dir_config*cfg;etag_components_tetag_bits;etag_components_tbits_added;cfg=(core_dir_config*)ap_get_module_config(r->per_dir_config,&core_module);etag_bits=(cfg->etag_bits&(~cfg->etag_remove))|cfg->etag_add;if(etag_bits&ETAG_NONE){apr_table_setn(r->notes,no-etag,omit);return;}if(etag_bits==ETAG_UNSET){etag_bits=ETAG_BACKWARD;}if((r->request_time-r->mtime>(1*APR_USEC_PER_SEC))&&!force_weak){weak=NULL;weak_len=0;}else{weak=ETAG_WEAK;weak_len=sizeof(ETAG_WEAK);}if(r->finfo.filetype!=0){etag=apr_palloc(r->pool,weak_len+sizeof(--)+3*CHARS_PER_UNSIGNED_LONG+1);next=etag;if(weak){while(*weak){*next++=*weak++;}}*next++='';bits_added=0;if(etag_bits&ETAG_INODE){next=etag_ulong_to_hex(next,(unsignedlong)r->finfo.inode);bits_added|=ETAG_INODE;}if(etag_bits&ETAG_SIZE){if(bits_added!=0){*next++='-';}next=etag_ulong_to_hex(next,(unsignedlong)r->finfo.size);bits_added|=ETAG_SIZE;}if(etag_bits&ETAG_MTIME){if(bits_added!=0){*next++='-';}next=etag_ulong_to_hex(next,(unsignedlong)r->mtime);}*next++='';*next='0';}else{etag=apr_palloc(r->pool,weak_len+sizeof()+CHARS_PER_UNSIGNED_LONG+1);next=etag;if(weak){while(*weak){*next++=*weak++;}}*next++='';next=etag_ulong_to_hex(next,(unsignedlong)r->mtime);*next++='';*next='0';}returnetag;}AP_DECLARE(void)ap_set_etag(request_rec*r){char*etag;char*variant_etag,*vlv;intvlv_weak;if(!r->vlist_validator){etag=ap_make_etag(r,0);if(!etag[0]){return;}}else{vlv=r->vlist_validator;vlv_weak=(vlv[0]=='W');variant_etag=ap_make_etag(r,vlv_weak);if(!variant_etag[0]){return;}variant_etag[strlen(variant_etag)-1]='0';if(vlv_weak){vlv+=3;}else{vlv++;}etag=apr_pstrcat(r->pool,variant_etag,;,vlv,NULL);}apr_table_setn(r->headers_out,ETag,etag);}staticintparse_byterange(char*range,apr_off_tclength,apr_off_t*start,apr_off_t*end){char*dash=strchr(range,'-');char*errp;apr_off_tnumber;if(!dash){return0;}if((dash==range)){if(apr_strtoff(&number,dash+1,&errp,10)||*errp){return0;}*start=clength-number;*end=clength-1;}else{*dash++='0';if(apr_strtoff(&number,range,&errp,10)||*errp){return0;}*start=number;if(*dash){if(apr_strtoff(&number,dash,&errp,10)||*errp){return0;}*end=number;}else{*end=clength-1;}}if(*start<0){*start=0;}if(*end>=clength){*end=clength-1;}if(*start>*end){return-1;}return(*start>0||*end<clength);}staticintap_set_byterange(request_rec*r);typedefstructbyterange_ctx{apr_bucket_brigade*bb;intnum_ranges;char*boundary;char*bound_head;}byterange_ctx;staticintuse_range_x(request_rec*r){constchar*ua;return(apr_table_get(r->headers_in,Request-Range)||((ua=apr_table_get(r->headers_in,User-Agent))&&ap_strstr_c(ua,MSIE3)));}#defineBYTERANGE_FMT%APR_OFF_T_FMT-%APR_OFF_T_FMT/%APR_OFF_T_FMT#definePARTITION_ERR_FMTapr_brigade_partition()failed[%APR_OFF_T_FMT,%APR_OFF_T_FMT]AP_CORE_DECLARE_NONSTD(apr_status_t)ap_byterange_filter(ap_filter_t*f,apr_bucket_brigade*bb){#defineMIN_LENGTH(len1,len2)((len1>len2)?len2:len1)request_rec*r=f->r;conn_rec*c=r->connection;byterange_ctx*ctx=f->ctx;apr_bucket*e;apr_bucket_brigade*bsend;apr_off_trange_start;apr_off_trange_end;char*current;apr_off_tbb_length;apr_off_tclength=0;apr_status_trv;intfound=0;if(!ctx){intnum_ranges=ap_set_byterange(r);if(num_ranges==0){ap_remove_output_filter(f);returnap_pass_brigade(f->next,bb);}ctx=f->ctx=apr_pcalloc(r->pool,sizeof(*ctx));ctx->num_ranges=num_ranges;ctx->bb=apr_brigade_create(r->pool,c->bucket_alloc);if(ctx->num_ranges>1){constchar*orig_ct=ap_make_content_type(r,r->content_type);ctx->boundary=apr_psprintf(r->pool,%APR_UINT64_T_HEX_FMT%lx,(apr_uint64_t)r->request_time,(long)getpid());ap_set_content_type(r,apr_pstrcat(r->pool,multipart,use_range_x(r)?/x-:/,byteranges;boundary=,ctx->boundary,NULL));ctx->bound_head=apr_pstrcat(r->pool,CRLF--,ctx->boundary,CRLFContent-type:,orig_ct,CRLFContent-range:bytes,NULL);ap_xlate_proto_to_ascii(ctx->bound_head,strlen(ctx->bound_head));}}if(!APR_BUCKET_IS_EOS(APR_BRIGADE_LAST(bb))){ap_save_brigade(f,&ctx->bb,&bb,r->pool);returnAPR_SUCCESS;}APR_BRIGADE_PREPEND(bb,ctx->bb);apr_brigade_length(bb,1,&bb_length);clength=(apr_off_t)bb_length;bsend=apr_brigade_create(r->pool,c->bucket_alloc);while((current=ap_getword(r->pool,&r->range,','))&&(rv=parse_byterange(current,clength,&range_start,&range_end))){apr_bucket*e2;apr_bucket*ec;if(rv==-1){continue;}if((rv=apr_brigade_partition(bb,range_start,&ec))!=APR_SUCCESS){ap_log_rerror(APLOG_MARK,APLOG_ERR,rv,r,PARTITION_ERR_FMT,range_start,clength);continue;}if((rv=apr_brigade_partition(bb,range_end+1,&e2))!=APR_SUCCESS){ap_log_rerror(APLOG_MARK,APLOG_ERR,rv,r,PARTITION_ERR_FMT,range_end+1,clength);continue;}found=1;if(ctx->num_ranges==1){apr_table_setn(r->headers_out,Content-Range,apr_psprintf(r->pool,bytesBYTERANGE_FMT,range_start,range_end,clength));}else{char*ts;e=apr_bucket_pool_create(ctx->bound_head,strlen(ctx->bound_head),r->pool,c->bucket_alloc);APR_BRIGADE_INSERT_TAIL(bsend,e);ts=apr_psprintf(r->pool,BYTERANGE_FMTCRLFCRLF,range_start,range_end,clength);ap_xlate_proto_to_ascii(ts,strlen(ts));e=apr_bucket_pool_create(ts,strlen(ts),r->pool,c->bucket_alloc);APR_BRIGADE_INSERT_TAIL(bsend,e);}do{apr_bucket*foo;constchar*str;apr_size_tlen;if(apr_bucket_copy(ec,&foo)!=APR_SUCCESS){apr_bucket_read(ec,&str,&len,APR_BLOCK_READ);apr_bucket_copy(ec,&foo);}APR_BRIGADE_INSERT_TAIL(bsend,foo);ec=APR_BUCKET_NEXT(ec);}while(ec!=e2);}if(found==0){ap_remove_output_filter(f);r->status=HTTP_OK;e=ap_bucket_error_create(HTTP_RANGE_NOT_SATISFIABLE,NULL,r->pool,c->bucket_alloc);APR_BRIGADE_INSERT_TAIL(bsend,e);e=apr_bucket_eos_create(c->bucket_alloc);APR_BRIGADE_INSERT_TAIL(bsend,e);returnap_pass_brigade(f->next,bsend);}if(ctx->num_ranges>1){char*end;end=apr_pstrcat(r->pool,CRLF--,ctx->boundary,--CRLF,NULL);ap_xlate_proto_to_ascii(end,strlen(end));e=apr_bucket_pool_create(end,strlen(end),r->pool,c->bucket_alloc);APR_BRIGADE_INSERT_TAIL(bsend,e);}e=apr_bucket_eos_create(c->bucket_alloc);APR_BRIGADE_INSERT_TAIL(bsend,e);apr_brigade_destroy(bb);returnap_pass_brigade(f->next,bsend);}staticintap_set_byterange(request_rec*r){constchar*range;constchar*if_range;constchar*match;constchar*ct;intnum_ranges;if(r->assbackwards){return0;}if(!(range=apr_table_get(r->headers_in,Range))){range=apr_table_get(r->headers_in,Request-Range);}if(!range||strncasecmp(range,bytes=,6)||r->status!=HTTP_OK){return0;}if(apr_table_get(r->headers_out,Content-Range)){return0;}if((ct=apr_table_get(r->headers_out,Content-Type))&&(!strncasecmp(ct,multipart/byteranges,20)||!strncasecmp(ct,multipart/x-byteranges,22))){return0;}if((if_range=apr_table_get(r->headers_in,If-Range))){if(if_range[0]==''){if(!(match=apr_table_get(r->headers_out,Etag))||(strcmp(if_range,match)!=0)){return0;}}elseif(!(match=apr_table_get(r->headers_out,Last-Modified))||(strcmp(if_range,match)!=0)){return0;}}if(!ap_strchr_c(range,',')){num_ranges=1;}else{num_ranges=2;}r->status=HTTP_PARTIAL_CONTENT;r->range=range+6;returnnum_ranges;}");


		File vulnerableFile = new File(FILE);

		System.out.println("===Bisect check for " + CVE + ", " + FILE + "===");
		try {
			if (isVulnerable(vulnerableFile)) {
				System.out.println("===VULNERABLE===");
				System.exit(BAD_RETURN_CODE); // vulnerable --> commit was "bad"
												// --> abnormal termination
			} else {
				System.out.println("===NEUTRAL===");
				System.exit(GOOD_RETURN_CODE); // neutral --> commit was "good"
												// --> normal termination
			}
		} catch (IOException e) {
			System.err.println("===IOException! See stack trace below===");
			System.err.println("Vulnerable file: "
					+ vulnerableFile.getAbsolutePath());
			e.printStackTrace();
			System.exit(SKIP_RETURN_CODE);
		}
	}

	/**
	 * 
	 * @param file
	 * @return boolean good or bad commit
	 * @throws IOException
	 */
	private static boolean isVulnerable(File file) throws IOException {
		StringBuffer sb = readFile(file);

		String fileContent = removeComments(removeUnwantedChars(sb.toString()));

		if (hasAll(fileContent, oldBlocks) && hasNone(fileContent, newBlocks)) {
			return true; // It is vulnerable:
							// Contains some context from latest bad commit and
							// doesn't contain the fix.
		} else {
			return false; // It is not vulnerable:
							// Either contains the fix or doesn't contain
							// context from the latest bad commit.
		}
	}

	private static String removeUnwantedChars(String text) {
		return text.replace("\r", "").replace("\n", "").replace("\t", "")
				.replace(" ", "").replace("\\", "").replace("\"", "");
	}

	private static String removeComments(String text) {
		return text
		        // Matches this: "/* comment */"
				.replaceAll("/\\*(?:.)*?\\*/", "")
				// Matches this: "comment */"
				.replaceAll("^(?:.)*?\\*/", "")
				// Matches this: "/* comment"
				.replaceAll("/\\*(?:.)*?$", "");
	}

	private static StringBuffer readFile(File fileName)
			throws FileNotFoundException, IOException {
		FileInputStream fstream = new FileInputStream(fileName);
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine;
		StringBuffer sb = new StringBuffer();
		while ((strLine = br.readLine()) != null) {
			sb.append(strLine.trim());
		}
		in.close();
		return sb;
	}

	private static boolean hasNone(String fileContent, List<String> mustNotHave) {
		for (String text : mustNotHave) {
			if (has(fileContent, text)) {
				return false;
			}
		}
		return true;
	}

	private static boolean hasAll(String fileContent, List<String> list) {
		for (String text : list) {
			if (!has(fileContent, text)) {
				return false;
			}
		}
		return true;
	}

	private static boolean has(String fileContent, String str) {
		boolean has = fileContent.indexOf(str) > 0;
		if (!has)
			System.out.println("\tContext not found: " + str);
		return has;
	}
}

