package tms.gis.tcs.stc.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;

import tms.com.rest.exception.RESTException;

import protocol.entity.PoiInfo;

import tms.gis.app.report.service.TMUSearchReportService;

import protocol.entity.PoiInfos;
import tms.com.rest.client.ClientResponse;
import tms.com.rest.exception.ExceptionManager;
import tms.gis.app.com.service.ComAppService;
import tms.gis.app.stc.service.StcAppService;
import tms.gis.biz.svc.vo.ProtocolHeaderInfoVO;
import tms.gis.biz.svc.vo.ServiceTwowayVO;
import tms.gis.com.constants.CommonConstants;
import tms.gis.com.exception.ExceptionMessageUtil;
import tms.gis.com.service.HServerResource;
import tms.gis.com.util.CommonUtil;


/**
 * This class for ASync Service From GIS Server To TMU, with Notification Server<BR>
 *<BR>
 * <pre>
 * ClassName   : StccASyncServerResource.java
 * Scenario    : 
 * Modification Information
 *
 *     since          author              description
 *  ===========    =============    ===========================
 *  Jan 25, 2013   kwihyun.ko       First Gen...
 * </pre>
 * @author kwihyun.ko
 * @since Jan 25, 2013
 * @version 1.0
 */
@Controller("stcASyncServerResource")
public class StcASyncServerResource extends HServerResource {

    @Autowired
    private ComAppService comAppService;

    @Autowired
    private StcAppService stcAppService;
    
    @Autowired 
    private TMUSearchReportService searchRptService;
    
    /**
     * First, Insert Service Request Info. then Send to TMU.<BR>
     * It will be Update Service Two Way Infomation when receive result from TMU.<BR> 
     *<BR>
     * Latest Will be send to Notification Server matter TMU result.<BR>
     * <BR>
     * <pre>
     * Message ID  : STC-02
     * Modification Information
     *
     *     since          author              description
     *  ===========    =============    ===========================
     *  Jan 25, 2013   kwihyun.ko       First Gen...
     * </pre>
     * @author kwihyun.ko
     * @since Jan 25, 2013
     * @version 1.0
     * 
     * @param protocolHeaderInfoVO
     * @param requestServiceTwowayVO
     * @param poiInfos
     */
    @Async
    public void receiveSendToCarRequest(ProtocolHeaderInfoVO protocolHeaderInfoVO,
            PoiInfos poiInfos, String messageId) throws Exception{
        
        Thread.sleep(1000);

        try {
            
           
            // It will change it to debug after the Hisna testing, and they send us the placeId  
            log.info("================================================================================");
            log.info(">>>>> Start Send To Car Async");
            log.info(" Tid " + protocolHeaderInfoVO.getTid() );
            for( PoiInfo poiInfo: poiInfos.getPoiInfoList()){
               if(poiInfo != null)
                   log.info( poiInfo.getPlaceid());
            }
            log.info("================================================================================");
            
          
            
            /* Insert Protocol Service Twoway Information */
            ServiceTwowayVO serviceTwowayVO = comAppService.insertServiceTwoway(protocolHeaderInfoVO, CommonConstants.GIS , messageId);
        
            /* Insert to Protocol Header Information */
            comAppService.insertProtocolHeaderInfo(protocolHeaderInfoVO, serviceTwowayVO);

            
            /**
             * Check Service VBR
             * 2014. 06. 27  Midified about TMU SWAP  ( ENG-493 / ENG-475 ) Modified By Yonghee, Park
             */
            int checkVBR =  comAppService.checkVbrInfomation(protocolHeaderInfoVO);
            
            if(checkVBR != CommonConstants.VBR_CHECK_ACTIVE){
                
                /* Service Result Message Notification */
                comAppService.sendNotification(protocolHeaderInfoVO, CommonConstants.VBR_CHECK_NOVBR, CommonConstants.SVC_SCENARIO_NAME.SEND_TO_CAR);

                if (checkVBR == CommonConstants.VBR_CHECK_INACTIVE){
            		comAppService.updateServiceTwoway(serviceTwowayVO, CommonConstants.SERVICE_REQUEST.SERVICE_INACTIVE);
            	}else{
            		comAppService.updateServiceTwoway(serviceTwowayVO, CommonConstants.SERVICE_REQUEST.NOVBR);
            	}
                searchRptService.insertSendToCarPOI(protocolHeaderInfoVO, poiInfos, "FAILURE");
                throw ExceptionManager.createRestException(checkVBR);
            }

            /* 1. Check Service Avaliable */
            boolean serviceAvaliable =  comAppService.checkServiceAvailable(protocolHeaderInfoVO,CommonConstants.STC);
            if(!serviceAvaliable){
                
                if(log.isDebugEnabled()) {
                    log.debug("Check Service Avaliable=========================================================");
                    log.debug("serviceAvaliable : "+ serviceAvaliable );
                    log.debug("================================================================================");
                }

                /* Update to STC Req Protocol Service Twoway Information */
                comAppService.updateServiceTwoway(serviceTwowayVO, CommonConstants.SERVICE_REQUEST.FAIL);

                /* Service Result Message Notification */
                comAppService.sendNotification(protocolHeaderInfoVO, 537, CommonConstants.SVC_SCENARIO_NAME.SEND_TO_CAR);
                searchRptService.insertSendToCarPOI(protocolHeaderInfoVO, poiInfos, "FAILURE");
                throw ExceptionManager.createRestException(537);    //Invalid Service
            }

            /* 2. StolenVehicleRecovery Status Check */
            String svrStatus = CommonUtil.chngNullStr(comAppService.checkStolenVehicleRecoveryStatus(protocolHeaderInfoVO, serviceTwowayVO));
            if(!CommonConstants.SVR_STATUS.RECOVERY.equals(svrStatus) && !"".equals(svrStatus)){

                if(log.isDebugEnabled()) {
                    log.debug("StolenVehicleRecovery Status====================================================");
                    log.debug("svrStatus : "+ svrStatus );
                    log.debug("================================================================================");
                }
                
                /* Update to STC Req Protocol Service Twoway Information */
                comAppService.updateServiceTwoway(serviceTwowayVO, CommonConstants.SERVICE_REQUEST.FAIL);

                /* Service Result Message Notification */
                comAppService.sendNotification(protocolHeaderInfoVO, 531, CommonConstants.SVC_SCENARIO_NAME.SEND_TO_CAR);
                searchRptService.insertSendToCarPOI(protocolHeaderInfoVO, poiInfos, "FAILURE");
                throw ExceptionManager.createRestException(531);    //While SVR Service being in progress, Others services are not work.
            }
            
            /* 3.Check Service Repeate Time */
            boolean checkRepeatedly = comAppService.checkRepeatedlyRequestService(protocolHeaderInfoVO, serviceTwowayVO);
            if(!checkRepeatedly) {
                
                if(log.isDebugEnabled()) {
                    log.debug("Check Service Repeate Time======================================================");
                    log.debug("checkRepeatedly : "+ checkRepeatedly);
                    log.debug("================================================================================");
                }

                /* Update to STC Req Protocol Service Twoway Information */
                comAppService.updateServiceTwoway(serviceTwowayVO, CommonConstants.SERVICE_REQUEST.REPEAT);

                /* Service Result Message Notification */
                comAppService.sendNotification(protocolHeaderInfoVO, 533, CommonConstants.SVC_SCENARIO_NAME.SEND_TO_CAR);
                searchRptService.insertSendToCarPOI(protocolHeaderInfoVO, poiInfos, "FAILURE");
                throw ExceptionManager.createRestException(533);
            }
            
            /* 4. Check Service 30 times a day */
            boolean checkSuccessCount = comAppService.checkSuccessCountServiceRequest(protocolHeaderInfoVO, serviceTwowayVO);
            if(!checkSuccessCount) {

                if(log.isDebugEnabled()) {
                    log.debug("Check Service 30 times a day====================================================");
                    log.debug("checkSuccessCount : "+checkSuccessCount);
                    log.debug("================================================================================");
                }

                /* Update to STC Req Protocol Service Twoway Information */
                comAppService.updateServiceTwoway(serviceTwowayVO, CommonConstants.SERVICE_REQUEST.REPEAT);

                /* Service Result Message Notification */
                comAppService.sendNotification(protocolHeaderInfoVO, 534, CommonConstants.SVC_SCENARIO_NAME.SEND_TO_CAR);
                searchRptService.insertSendToCarPOI(protocolHeaderInfoVO, poiInfos, "FAILURE");
                throw ExceptionManager.createRestException(534);
            }
            
            //GOOGLE  Header
            ProtocolHeaderInfoVO stc02HeaderInfoVO = comAppService.setSendHeaderInfo(protocolHeaderInfoVO, CommonConstants.SVC_MESSAGE_URI.STC02_URI, CommonConstants.HTTP_METHOD.POST, CommonConstants.TMU);
            
            /* Insert STC-02 Service Request */
            ServiceTwowayVO stc02ServiceTwowayVO = comAppService.insertServiceTwoway(protocolHeaderInfoVO, CommonConstants.GIS,  CommonConstants.STC02, CommonConstants.TMU);
    
            /**
             * Insert to ScenarioTransaction Info
             */
            comAppService.insertScenarioTransaction(stc02HeaderInfoVO, stc02ServiceTwowayVO, poiInfos);
            
            /**
             *  Insert to Protocol Header Information
             */
            comAppService.insertProtocolHeaderInfo(stc02HeaderInfoVO, stc02ServiceTwowayVO);
            
            /** for local test **/
//            protocolHeaderInfoVO.setTelecom("SKT");
            
            ClientResponse<Object> clientResponse = stcAppService.sendStc02(stc02HeaderInfoVO, serviceTwowayVO, poiInfos);
    
            protocolHeaderInfoVO = comAppService.setHeaderInfo(clientResponse.getHeaders(), protocolHeaderInfoVO);
            
            
            if(HttpStatus.OK.value() == clientResponse.getStatus() || HttpStatus.NO_CONTENT.value() == clientResponse.getStatus()) {
                
                /* Update to STC-02 Protocol Service Twoway Information */
                comAppService.updateServiceTwoway(stc02ServiceTwowayVO, CommonConstants.SERVICE_REQUEST.SUCCESS);
                
                /* Update to [STC-01, STC-03, STC-04] Protocol Service Twoway Information */
                comAppService.updateServiceTwoway(serviceTwowayVO, CommonConstants.SERVICE_REQUEST.SUCCESS);
                
                /* Service Result Message Notification */
                comAppService.sendNotification(protocolHeaderInfoVO, clientResponse.getStatus(), CommonConstants.SVC_SCENARIO_NAME.SEND_TO_CAR);
               
                searchRptService.insertSendToCarPOI(protocolHeaderInfoVO, poiInfos, "SUCCESS");
            } else {
    
                if (log.isDebugEnabled()) {
                    log.debug("================================================================================");
                    log.debug("clientResponse.getStatus():"+clientResponse.getStatus());
                    log.debug("================================================================================");
                }
                
                /* Update to STC-02 Protocol Service Twoway Information */
                comAppService.updateServiceTwoway(stc02ServiceTwowayVO, CommonConstants.SERVICE_REQUEST.FAIL);
                
                /* Update to [STC-01, STC-03, STC-04] Protocol Service Twoway Information */
                comAppService.updateServiceTwoway(serviceTwowayVO, CommonConstants.SERVICE_REQUEST.FAIL);
                
                /* Service Result Message Notification */
                comAppService.sendNotification(protocolHeaderInfoVO, clientResponse.getStatus(), CommonConstants.SVC_SCENARIO_NAME.SEND_TO_CAR);
                
                searchRptService.insertSendToCarPOI(protocolHeaderInfoVO, poiInfos, "FAILURE");
                
                throw ExceptionManager.createRestException(clientResponse.getStatus());
                
            }
            
            log.debug("================================================================================");
            log.debug(">>>>> End Send To Car Async" + protocolHeaderInfoVO.getTid());
            log.debug("================================================================================");

        } catch (RESTException e) {
            if (log.isDebugEnabled()) {
                log.debug("[" + protocolHeaderInfoVO.getTid() + "][EX]");
                log.error("[" + protocolHeaderInfoVO.getTid() + "]"
                        + ExceptionMessageUtil.getExceptionDetailMsg(e));
            }
            
            throw ExceptionManager.createRestException(e.getStatusCode());
           
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.error("["+protocolHeaderInfoVO.getTwowayId()+"][EX]");
                log.error(e.getMessage());
                log.error("["+protocolHeaderInfoVO.getTwowayId()+"]" + ExceptionMessageUtil.getExceptionDetailMsg(e));
            }
            throw ExceptionManager.createRestException(500);
        }
        
    }
}
