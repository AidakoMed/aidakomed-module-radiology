/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.radiology.web.controller;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.OrderService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.radiology.MwlStatus;
import org.openmrs.module.radiology.PerformedProcedureStepStatus;
import org.openmrs.module.radiology.RadiologyOrder;
import org.openmrs.module.radiology.RadiologyProperties;
import org.openmrs.module.radiology.RadiologyService;
import org.openmrs.module.radiology.Study;
import org.openmrs.module.radiology.test.RadiologyTestData;
import org.openmrs.test.BaseContextMockTest;
import org.openmrs.test.Verifies;
import org.openmrs.web.WebConstants;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;

/**
 * Tests {@link RadiologyOrderFormController}
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(RadiologyProperties.class)
@PowerMockIgnore( { "org.apache.commons.logging.*" })
public class RadiologyOrderFormControllerTest extends BaseContextMockTest {
	
	@Mock
	private PatientService patientService;
	
	@Mock
	private OrderService orderService;
	
	@Mock
	private RadiologyService radiologyService;
	
	@Mock
	private MessageSourceService messageSourceService;
	
	@Mock
	private AdministrationService administrationService;
	
	@InjectMocks
	private RadiologyOrderFormController radiologyOrderFormController = new RadiologyOrderFormController();
	
	@Before
	public void runBeforeAllTests() {
		PowerMockito.mockStatic(RadiologyProperties.class);
		
		when(RadiologyProperties.getRadiologyTestOrderType()).thenReturn(RadiologyTestData.getMockRadiologyOrderType());
	}
	
	/**
	 * @see RadiologyOrderFormController#getRadiologyOrderFormWithNewOrder()
	 */
	@Test
	@Verifies(value = "should populate model and view with new order and study", method = "getRadiologyOrderFormWithNewOrder()")
	public void getRadiologyOrderFormWithNewOrder_shouldPopulateModelAndViewWithNewOrderAndStudy() throws Exception {
		
		ModelAndView modelAndView = radiologyOrderFormController.getRadiologyOrderFormWithNewOrder();
		
		assertNotNull(modelAndView);
		assertThat(modelAndView.getViewName(), is("module/radiology/radiologyOrderForm"));
		
		assertTrue(modelAndView.getModelMap().containsKey("study"));
		Study study = (Study) modelAndView.getModelMap().get("study");
		assertNull(study.getStudyId());
		
		assertTrue(modelAndView.getModelMap().containsKey("order"));
		RadiologyOrder order = (RadiologyOrder) modelAndView.getModelMap().get("order");
		assertNull(order.getOrderId());
		
		assertNull(order.getOrderer());
	}
	
	/**
	 * @see RadiologyOrderFormController#getRadiologyOrderFormWithNewOrder()
	 */
	@Test
	@Verifies(value = "should populate model and view with new order and study with prefilled orderer when requested by referring physician", method = "getRadiologyOrderFormWithNewOrder()")
	public void getRadiologyOrderFormWithNewOrder_shouldPopulateModelAndViewWithNewOrderAndStudyWithPrefilledOrdererWhenRequestedByReferringPhysician()
	        throws Exception {
		
		//given
		User mockReferringPhysician = RadiologyTestData.getMockRadiologyReferringPhysician();
		when(userContext.getAuthenticatedUser()).thenReturn(mockReferringPhysician);
		
		ModelAndView modelAndView = radiologyOrderFormController.getRadiologyOrderFormWithNewOrder();
		
		assertNotNull(modelAndView);
		assertThat(modelAndView.getViewName(), is("module/radiology/radiologyOrderForm"));
		
		assertTrue(modelAndView.getModelMap().containsKey("study"));
		Study study = (Study) modelAndView.getModelMap().get("study");
		assertNull(study.getStudyId());
		
		assertTrue(modelAndView.getModelMap().containsKey("order"));
		RadiologyOrder order = (RadiologyOrder) modelAndView.getModelMap().get("order");
		assertNull(order.getOrderId());
		
		assertNotNull(order.getOrderer());
		assertThat(order.getOrderer(), is(mockReferringPhysician));
	}
	
	/**
	 * @see RadiologyOrderFormController#getRadiologyOrderFormWithNewOrderAndPrefilledPatient(Integer)
	 */
	@Test
	@Verifies(value = "should populate model and view with new order and study prefilled with given patient", method = "getRadiologyOrderFormWithNewOrderAndPrefilledPatient(Integer)")
	public void getRadiologyOrderFormWithNewOrderAndPrefilledPatient_shouldPopulateModelAndViewWithNewOrderAndStudyPrefilledWithGivenPatient()
	        throws Exception {
		
		//given
		Patient mockPatient = RadiologyTestData.getMockPatient1();
		
		ModelAndView modelAndView = radiologyOrderFormController
		        .getRadiologyOrderFormWithNewOrderAndPrefilledPatient(mockPatient);
		
		assertNotNull(modelAndView);
		assertThat(modelAndView.getViewName(), is("module/radiology/radiologyOrderForm"));
		
		assertTrue(modelAndView.getModelMap().containsKey("study"));
		Study study = (Study) modelAndView.getModelMap().get("study");
		assertNull(study.getStudyId());
		
		assertTrue(modelAndView.getModelMap().containsKey("order"));
		RadiologyOrder order = (RadiologyOrder) modelAndView.getModelMap().get("order");
		assertNull(order.getOrderId());
		
		assertNotNull(order.getPatient());
		assertThat(order.getPatient(), is(mockPatient));
		
		assertTrue(modelAndView.getModelMap().containsKey("patientId"));
		Integer patientId = (Integer) modelAndView.getModelMap().get("patientId");
		assertThat(patientId, is(mockPatient.getPatientId()));
	}
	
	/**
	 * @see RadiologyOrderFormController#getRadiologyOrderFormWithNewOrderAndPrefilledPatient(Integer)
	 */
	@Test
	@Verifies(value = "should populate model and view with new order and study with prefilled orderer when requested by referring physician", method = "getRadiologyOrderFormWithNewOrderAndPrefilledPatient(Integer)")
	public void getRadiologyOrderFormWithNewOrderAndPrefilledPatient_shouldPopulateModelAndViewWithNewOrderAndStudyWithPrefilledOrdererWhenRequestedByReferringPhysician()
	        throws Exception {
		
		//given
		Patient mockPatient = RadiologyTestData.getMockPatient1();
		User mockReferringPhysician = RadiologyTestData.getMockRadiologyReferringPhysician();
		
		when(userContext.getAuthenticatedUser()).thenReturn(mockReferringPhysician);
		when(patientService.getPatient(mockPatient.getPatientId())).thenReturn(mockPatient);
		
		ModelAndView modelAndView = radiologyOrderFormController
		        .getRadiologyOrderFormWithNewOrderAndPrefilledPatient(mockPatient);
		
		assertNotNull(modelAndView);
		assertThat(modelAndView.getViewName(), is("module/radiology/radiologyOrderForm"));
		
		assertTrue(modelAndView.getModelMap().containsKey("study"));
		Study study = (Study) modelAndView.getModelMap().get("study");
		assertNull(study.getStudyId());
		
		assertTrue(modelAndView.getModelMap().containsKey("order"));
		RadiologyOrder order = (RadiologyOrder) modelAndView.getModelMap().get("order");
		assertNull(order.getOrderId());
		
		assertNotNull(order.getOrderer());
		assertThat(order.getOrderer(), is(mockReferringPhysician));
	}
	
	/**
	 * @see RadiologyOrderFormController#getRadiologyOrderFormWithNewOrderAndPrefilledPatient(Integer)
	 */
	@Test
	@Verifies(value = "should populate model and view with new order and study without prefilled patient if given patient is null", method = "getRadiologyOrderFormWithNewOrderAndPrefilledPatient(Integer)")
	public void getRadiologyOrderFormWithNewOrderAndPrefilledPatient_shouldPopulateModelAndViewWithNewOrderAndStudyWithoutPrefilledPatientIfGivenPatientIsNull()
	        throws Exception {
		
		ModelAndView modelAndView = radiologyOrderFormController.getRadiologyOrderFormWithNewOrderAndPrefilledPatient(null);
		
		assertNotNull(modelAndView);
		assertThat(modelAndView.getViewName(), is("module/radiology/radiologyOrderForm"));
		
		assertTrue(modelAndView.getModelMap().containsKey("study"));
		Study study = (Study) modelAndView.getModelMap().get("study");
		assertNull(study.getStudyId());
		
		assertTrue(modelAndView.getModelMap().containsKey("order"));
		RadiologyOrder order = (RadiologyOrder) modelAndView.getModelMap().get("order");
		assertNull(order.getOrderId());
		
		assertNull(order.getPatient());
		
		assertFalse(modelAndView.getModelMap().containsKey("patientId"));
	}
	
	/**
	 * @see RadiologyOrderFormController#getRadiologyOrderFormWithExistingOrderByOrderId(Integer)
	 */
	@Test
	@Verifies(value = "should populate model and view with existing order and study matching given order id", method = "getRadiologyOrderFormWithExistingOrderByOrderId(Integer)")
	public void getRadiologyOrderFormWithExistingOrder_shouldPopulateModelAndViewWithExistingOrderAndStudyMatchingGivenOrderId()
	        throws Exception {
		
		//given
		RadiologyOrder mockRadiologyOrder = RadiologyTestData.getMockRadiologyOrder1();
		Study mockStudy = RadiologyTestData.getMockStudy1PostSave();
		
		when(radiologyService.getRadiologyOrderByOrderId(mockRadiologyOrder.getOrderId())).thenReturn(mockRadiologyOrder);
		when(radiologyService.getStudyByOrderId(mockRadiologyOrder.getOrderId())).thenReturn(mockStudy);
		
		ModelAndView modelAndView = radiologyOrderFormController
		        .getRadiologyOrderFormWithExistingOrderByOrderId(mockRadiologyOrder.getOrderId());
		
		assertNotNull(modelAndView);
		assertThat(modelAndView.getViewName(), is("module/radiology/radiologyOrderForm"));
		
		assertTrue(modelAndView.getModelMap().containsKey("study"));
		Study study = (Study) modelAndView.getModelMap().get("study");
		assertThat(study, is(mockStudy));
		
		assertTrue(modelAndView.getModelMap().containsKey("order"));
		RadiologyOrder order = (RadiologyOrder) modelAndView.getModelMap().get("order");
		assertThat(order, is(mockRadiologyOrder));
	}
	
	/**
	 * @see RadiologyOrderFormController#postSaveOrder(Integer, Study, BindingResult, Order,
	 *      BindingResult)
	 */
	@Test
	@Verifies(value = "should set http session attribute openmrs message to order saved and redirect to radiology order list when save study was successful", method = "postSaveOrder(Integer, Study, BindingResult, Order, BindingResult)")
	public void postSaveOrder_shouldSetHttpSessionAttributeOpenmrsMessageToOrderSavedAndRedirectToRadiologyOrderListWhenSaveStudyWasSuccessful()
	        throws Exception {
		
		//given
		RadiologyOrder mockRadiologyOrder = RadiologyTestData.getMockRadiologyOrder1();
		Study mockStudyPreSave = RadiologyTestData.getMockStudy1PreSave();
		Study mockStudyPostSave = RadiologyTestData.getMockStudy1PostSave();
		mockStudyPostSave.setMwlStatus(MwlStatus.SAVE_OK);
		
		when(radiologyService.saveRadiologyOrder(mockRadiologyOrder)).thenReturn(mockRadiologyOrder);
		when(radiologyService.saveStudy(mockStudyPreSave)).thenReturn(mockStudyPostSave);
		when(radiologyService.getStudy(mockStudyPostSave.getStudyId())).thenReturn(mockStudyPostSave);
		
		MockHttpServletRequest mockRequest = new MockHttpServletRequest();
		mockRequest.addParameter("saveOrder", "saveOrder");
		MockHttpSession mockSession = new MockHttpSession();
		mockRequest.setSession(mockSession);
		
		BindingResult studyErrors = mock(BindingResult.class);
		when(studyErrors.hasErrors()).thenReturn(false);
		BindingResult orderErrors = mock(BindingResult.class);
		when(orderErrors.hasErrors()).thenReturn(false);
		
		ModelAndView modelAndView = radiologyOrderFormController.postSaveOrder(mockRequest, null, mockStudyPreSave,
		    studyErrors, mockRadiologyOrder, orderErrors);
		
		assertNotNull(modelAndView);
		assertThat(modelAndView.getViewName(), is("redirect:/module/radiology/radiologyOrder.list"));
		assertThat((String) mockSession.getAttribute(WebConstants.OPENMRS_MSG_ATTR), is("Order.saved"));
	}
	
	/**
	 * @see RadiologyOrderFormController#postSaveOrder(Integer, Study, BindingResult, Order,
	 *      BindingResult)
	 */
	@Test
	@Verifies(value = "should set http session attribute openmrs message to order saved and redirect to patient dashboard when save study was successful and given patient id", method = "postSaveOrder(Integer, Study, BindingResult, Order, BindingResult)")
	public void postSaveOrder_shouldSetHttpSessionAttributeOpenmrsMessageToOrderSavedAndRedirectToPatientDashboardWhenSaveStudyWasSuccessfulAndGivenPatientId()
	        throws Exception {
		
		//given
		RadiologyOrder mockRadiologyOrder = RadiologyTestData.getMockRadiologyOrder1();
		Study mockStudyPreSave = RadiologyTestData.getMockStudy1PreSave();
		Study mockStudyPostSave = RadiologyTestData.getMockStudy1PostSave();
		mockStudyPostSave.setMwlStatus(MwlStatus.SAVE_OK);
		
		when(radiologyService.saveRadiologyOrder(mockRadiologyOrder)).thenReturn(mockRadiologyOrder);
		when(radiologyService.saveStudy(mockStudyPreSave)).thenReturn(mockStudyPostSave);
		when(radiologyService.getStudy(mockStudyPostSave.getStudyId())).thenReturn(mockStudyPostSave);
		
		MockHttpServletRequest mockRequest = new MockHttpServletRequest();
		mockRequest.addParameter("saveOrder", "saveOrder");
		MockHttpSession mockSession = new MockHttpSession();
		mockRequest.setSession(mockSession);
		
		BindingResult studyErrors = mock(BindingResult.class);
		when(studyErrors.hasErrors()).thenReturn(false);
		BindingResult orderErrors = mock(BindingResult.class);
		when(orderErrors.hasErrors()).thenReturn(false);
		
		ModelAndView modelAndView = radiologyOrderFormController.postSaveOrder(mockRequest, mockRadiologyOrder.getPatient()
		        .getPatientId(), mockStudyPreSave, studyErrors, mockRadiologyOrder, orderErrors);
		
		assertNotNull(modelAndView);
		assertThat(modelAndView.getViewName(), is("redirect:/patientDashboard.form?patientId="
		        + mockRadiologyOrder.getPatient().getPatientId()));
		assertThat((String) mockSession.getAttribute(WebConstants.OPENMRS_MSG_ATTR), is("Order.saved"));
	}
	
	/**
	 * @see RadiologyOrderFormController#postSaveOrder(Integer, Study, BindingResult, Order,
	 *      BindingResult)
	 */
	@Test
	@Verifies(value = "should set http session attribute openmrs message to saved fail worklist and redirect to patient dashboard when save study was not successful and given patient id", method = "postSaveOrder(Integer, Study, BindingResult, Order, BindingResult)")
	public void postSaveOrder_shouldSetHttpSessionAttributeOpenmrsMessageToSavedFailWorklistAndRedirectToPatientDashboardWhenSaveStudyWasNotSuccessfulAndGivenPatientId()
	        throws Exception {
		
		//given
		RadiologyOrder mockRadiologyOrder = RadiologyTestData.getMockRadiologyOrder1();
		Study mockStudyPreSave = RadiologyTestData.getMockStudy1PreSave();
		Study mockStudyPostSave = RadiologyTestData.getMockStudy1PostSave();
		mockStudyPostSave.setMwlStatus(MwlStatus.SAVE_ERR);
		
		when(radiologyService.saveRadiologyOrder(mockRadiologyOrder)).thenReturn(mockRadiologyOrder);
		when(radiologyService.saveStudy(mockStudyPreSave)).thenReturn(mockStudyPostSave);
		when(radiologyService.getStudy(mockStudyPostSave.getStudyId())).thenReturn(mockStudyPostSave);
		
		MockHttpServletRequest mockRequest = new MockHttpServletRequest();
		mockRequest.addParameter("saveOrder", "saveOrder");
		MockHttpSession mockSession = new MockHttpSession();
		mockRequest.setSession(mockSession);
		
		BindingResult orderErrors = mock(BindingResult.class);
		when(orderErrors.hasErrors()).thenReturn(false);
		BindingResult studyErrors = mock(BindingResult.class);
		when(studyErrors.hasErrors()).thenReturn(false);
		
		ModelAndView modelAndView = radiologyOrderFormController.postSaveOrder(mockRequest, mockRadiologyOrder.getPatient()
		        .getPatientId(), mockStudyPreSave, studyErrors, mockRadiologyOrder, orderErrors);
		
		assertNotNull(modelAndView);
		assertThat(modelAndView.getViewName(), is("redirect:/patientDashboard.form?patientId="
		        + mockRadiologyOrder.getPatient().getPatientId()));
		assertThat((String) mockSession.getAttribute(WebConstants.OPENMRS_MSG_ATTR), is("radiology.savedFailWorklist"));
		
		mockRequest = new MockHttpServletRequest();
		mockRequest.addParameter("saveOrder", "saveOrder");
		mockSession = new MockHttpSession();
		mockRequest.setSession(mockSession);
		
		mockStudyPostSave = RadiologyTestData.getMockStudy1PostSave();
		mockStudyPostSave.setMwlStatus(MwlStatus.UPDATE_ERR);
		when(radiologyService.saveStudy(mockStudyPreSave)).thenReturn(mockStudyPostSave);
		
		modelAndView = radiologyOrderFormController.postSaveOrder(mockRequest, mockRadiologyOrder.getPatient()
		        .getPatientId(), mockStudyPreSave, studyErrors, mockRadiologyOrder, orderErrors);
		
		assertNotNull(modelAndView);
		assertThat(modelAndView.getViewName(), is("redirect:/patientDashboard.form?patientId="
		        + mockRadiologyOrder.getPatient().getPatientId()));
		assertThat((String) mockSession.getAttribute(WebConstants.OPENMRS_MSG_ATTR), is("radiology.savedFailWorklist"));
	}
	
	/**
	 * @see RadiologyOrderFormController#postSaveOrder(Integer, Study, BindingResult, Order,
	 *      BindingResult)
	 */
	@Test
	@Verifies(value = "should set http session attribute openmrs message to study performed when study performed status is in progress and scheduler is empty and request was issued by radiology scheduler", method = "postSaveOrder(Integer, Study, BindingResult, Order, BindingResult)")
	public void postSaveOrder_shouldSetHttpSessionAttributeOpenmrsMessageToStudyPerformedWhenStudyPerformedStatusIsInProgressAndSchedulerIsEmptyAndRequestWasIssuedByRadiologyScheduler()
	        throws Exception {
		
		//given
		RadiologyOrder mockRadiologyOrder = RadiologyTestData.getMockRadiologyOrder1();
		Study mockStudyPreSave = RadiologyTestData.getMockStudy1PreSave();
		mockStudyPreSave.setPerformedStatus(PerformedProcedureStepStatus.IN_PROGRESS);
		Study mockStudyPostSave = RadiologyTestData.getMockStudy1PostSave();
		User mockRadiologyScheduler = RadiologyTestData.getMockRadiologyScheduler();
		
		when(userContext.getAuthenticatedUser()).thenReturn(mockRadiologyScheduler);
		when(radiologyService.saveRadiologyOrder(mockRadiologyOrder)).thenReturn(mockRadiologyOrder);
		when(radiologyService.saveStudy(mockStudyPreSave)).thenReturn(mockStudyPostSave);
		when(radiologyService.getStudy(mockStudyPostSave.getStudyId())).thenReturn(mockStudyPostSave);
		
		MockHttpServletRequest mockRequest = new MockHttpServletRequest();
		mockRequest.addParameter("saveOrder", "saveOrder");
		MockHttpSession mockSession = new MockHttpSession();
		mockRequest.setSession(mockSession);
		
		BindingResult studyErrors = mock(BindingResult.class);
		when(studyErrors.hasErrors()).thenReturn(false);
		BindingResult orderErrors = mock(BindingResult.class);
		when(orderErrors.hasErrors()).thenReturn(false);
		
		ModelAndView modelAndView = radiologyOrderFormController.postSaveOrder(mockRequest, mockRadiologyOrder.getPatient()
		        .getPatientId(), mockStudyPreSave, studyErrors, mockRadiologyOrder, orderErrors);
		
		assertNotNull(modelAndView);
		assertThat(modelAndView.getViewName(), is("module/radiology/radiologyOrderForm"));
		assertThat((String) mockSession.getAttribute(WebConstants.OPENMRS_ERROR_ATTR), is("radiology.studyPerformed"));
	}
	
	/**
	 * @see RadiologyOrderFormController#postSaveOrder(Integer, Study, BindingResult, Order,
	 *      BindingResult)
	 */
	@Test
	@Verifies(value = "should not redirect if order is not valid according to order validator", method = "postSaveOrder(Integer, Study, BindingResult, Order, BindingResult)")
	public void postSaveOrder_shouldNotRedirectIfOrderIsNotValidAccordingToOrderValidator() throws Exception {
		
		//given
		RadiologyOrder mockRadiologyOrder = RadiologyTestData.getMockRadiologyOrder1();
		Study mockStudyPreSave = RadiologyTestData.getMockStudy1PreSave();
		Study mockStudyPostSave = RadiologyTestData.getMockStudy1PostSave();
		User mockRadiologyScheduler = RadiologyTestData.getMockRadiologyScheduler();
		
		when(userContext.getAuthenticatedUser()).thenReturn(mockRadiologyScheduler);
		when(radiologyService.saveRadiologyOrder(mockRadiologyOrder)).thenReturn(mockRadiologyOrder);
		when(radiologyService.saveStudy(mockStudyPreSave)).thenReturn(mockStudyPostSave);
		when(radiologyService.getStudy(mockStudyPostSave.getStudyId())).thenReturn(mockStudyPostSave);
		
		MockHttpServletRequest mockRequest = new MockHttpServletRequest();
		mockRequest.addParameter("saveOrder", "saveOrder");
		MockHttpSession mockSession = new MockHttpSession();
		mockRequest.setSession(mockSession);
		
		BindingResult studyErrors = mock(BindingResult.class);
		when(studyErrors.hasErrors()).thenReturn(false);
		BindingResult orderErrors = mock(BindingResult.class);
		when(orderErrors.hasErrors()).thenReturn(true);
		
		ModelAndView modelAndView = radiologyOrderFormController.postSaveOrder(mockRequest, mockRadiologyOrder.getPatient()
		        .getPatientId(), mockStudyPreSave, studyErrors, mockRadiologyOrder, orderErrors);
		
		assertNotNull(modelAndView);
		assertThat(modelAndView.getViewName(), is("module/radiology/radiologyOrderForm"));
	}
	
	/**
	 * @see RadiologyOrderFormController#post(Integer, Study, BindingResult, Order, BindingResult)
	 */
	@Test
	@Verifies(value = "should set http session attribute openmrs message to voided successfully and redirect to patient dashboard when void order was successful and given patient id", method = "post(Integer, Study, BindingResult, Order, BindingResult)")
	public void post_shouldSetHttpSessionAttributeOpenmrsMessageToVoidedSuccessfullyAndRedirectToPatientDashboardWhenVoidOrderWasSuccessfulAndGivenPatientId()
	        throws Exception {
		
		//given
		RadiologyOrder mockRadiologyOrder = RadiologyTestData.getMockRadiologyOrder1();
		Study mockStudyPreSave = RadiologyTestData.getMockStudy1PreSave();
		Study mockStudyPostSave = RadiologyTestData.getMockStudy1PostSave();
		mockStudyPostSave.setMwlStatus(MwlStatus.VOID_OK);
		
		when(radiologyService.getRadiologyOrderByOrderId(mockRadiologyOrder.getOrderId())).thenReturn(mockRadiologyOrder);
		when(radiologyService.getStudyByOrderId(mockRadiologyOrder.getOrderId())).thenReturn(mockStudyPostSave);
		
		MockHttpServletRequest mockRequest = new MockHttpServletRequest();
		mockRequest.addParameter("voidOrder", "voidOrder");
		MockHttpSession mockSession = new MockHttpSession();
		mockRequest.setSession(mockSession);
		
		BindingResult studyErrors = mock(BindingResult.class);
		when(studyErrors.hasErrors()).thenReturn(false);
		BindingResult orderErrors = mock(BindingResult.class);
		when(orderErrors.hasErrors()).thenReturn(false);
		
		ModelAndView modelAndView = radiologyOrderFormController.post(mockRequest, mockRadiologyOrder.getPatient()
		        .getPatientId(), mockStudyPreSave, studyErrors, mockRadiologyOrder, orderErrors);
		
		assertNotNull(modelAndView);
		assertThat(modelAndView.getViewName(), is("redirect:/patientDashboard.form?patientId="
		        + mockRadiologyOrder.getPatient().getPatientId()));
		assertThat((String) mockSession.getAttribute(WebConstants.OPENMRS_MSG_ATTR), is("Order.voidedSuccessfully"));
	}
	
	/**
	 * @see RadiologyOrderFormController#post(Integer, Study, BindingResult, Order, BindingResult)
	 */
	@Test
	@Verifies(value = "should set http session attribute openmrs message to unvoided successfully and redirect to patient dashboard when unvoid order was successful and given patient id", method = "post(Integer, Study, BindingResult, Order, BindingResult)")
	public void post_shouldSetHttpSessionAttributeOpenmrsMessageToUnvoidedSuccessfullyAndRedirectToPatientDashboardWhenUnvoidOrderWasSuccessfulAndGivenPatientId()
	        throws Exception {
		
		//given
		RadiologyOrder mockRadiologyOrder = RadiologyTestData.getMockRadiologyOrder1();
		Study mockStudyPreSave = RadiologyTestData.getMockStudy1PreSave();
		Study mockStudyPostSave = RadiologyTestData.getMockStudy1PostSave();
		mockStudyPostSave.setMwlStatus(MwlStatus.UNVOID_OK);
		
		when(radiologyService.getRadiologyOrderByOrderId(mockRadiologyOrder.getOrderId())).thenReturn(mockRadiologyOrder);
		when(radiologyService.getStudyByOrderId(mockRadiologyOrder.getOrderId())).thenReturn(mockStudyPostSave);
		
		MockHttpServletRequest mockRequest = new MockHttpServletRequest();
		mockRequest.addParameter("unvoidOrder", "unvoidOrder");
		MockHttpSession mockSession = new MockHttpSession();
		mockRequest.setSession(mockSession);
		
		BindingResult studyErrors = mock(BindingResult.class);
		when(studyErrors.hasErrors()).thenReturn(false);
		BindingResult orderErrors = mock(BindingResult.class);
		when(orderErrors.hasErrors()).thenReturn(false);
		
		ModelAndView modelAndView = radiologyOrderFormController.post(mockRequest, mockRadiologyOrder.getPatient()
		        .getPatientId(), mockStudyPreSave, studyErrors, mockRadiologyOrder, orderErrors);
		
		assertNotNull(modelAndView);
		assertThat(modelAndView.getViewName(), is("redirect:/patientDashboard.form?patientId="
		        + mockRadiologyOrder.getPatient().getPatientId()));
		assertThat((String) mockSession.getAttribute(WebConstants.OPENMRS_MSG_ATTR), is("Order.unvoidedSuccessfully"));
	}
	
	/**
	 * @see RadiologyOrderFormController#post(Integer, Study, BindingResult, Order, BindingResult)
	 */
	@Test
	@Verifies(value = "should set http session attribute openmrs message to discontinued successfully and redirect to patient dashboard when discontinue order was successful and given patient id", method = "post(Integer, Study, BindingResult, Order, BindingResult)")
	public void post_shouldSetHttpSessionAttributeOpenmrsMessageToDiscontinuedSuccessfullyAndRedirectToPatientDashboardWhenDiscontinueOrderWasSuccessfulAndGivenPatientId()
	        throws Exception {
		
		//given
		RadiologyOrder mockRadiologyOrder = RadiologyTestData.getMockRadiologyOrder1();
		Study mockStudyPreSave = RadiologyTestData.getMockStudy1PreSave();
		Study mockStudyPostSave = RadiologyTestData.getMockStudy1PostSave();
		mockStudyPostSave.setMwlStatus(MwlStatus.DISCONTINUE_OK);
		
		when(radiologyService.getRadiologyOrderByOrderId(mockRadiologyOrder.getOrderId())).thenReturn(mockRadiologyOrder);
		when(radiologyService.getStudyByOrderId(mockRadiologyOrder.getOrderId())).thenReturn(mockStudyPostSave);
		
		MockHttpServletRequest mockRequest = new MockHttpServletRequest();
		mockRequest.addParameter("discontinueOrder", "discontinueOrder");
		MockHttpSession mockSession = new MockHttpSession();
		mockRequest.setSession(mockSession);
		
		BindingResult studyErrors = mock(BindingResult.class);
		when(studyErrors.hasErrors()).thenReturn(false);
		BindingResult orderErrors = mock(BindingResult.class);
		when(orderErrors.hasErrors()).thenReturn(false);
		
		ModelAndView modelAndView = radiologyOrderFormController.post(mockRequest, mockRadiologyOrder.getPatient()
		        .getPatientId(), mockStudyPreSave, studyErrors, mockRadiologyOrder, orderErrors);
		
		assertNotNull(modelAndView);
		assertThat(modelAndView.getViewName(), is("redirect:/patientDashboard.form?patientId="
		        + mockRadiologyOrder.getPatient().getPatientId()));
		assertThat((String) mockSession.getAttribute(WebConstants.OPENMRS_MSG_ATTR), is("Order.discontinuedSuccessfully"));
	}
	
	/**
	 * @see RadiologyOrderFormController#post(Integer, Study, BindingResult, Order, BindingResult)
	 */
	@Test
	@Verifies(value = "should set http session attribute openmrs message to undiscontinued successfully and redirect to patient dashboard when undiscontinue order was successful and given patient id", method = "post(Integer, Study, BindingResult, Order, BindingResult)")
	public void post_shouldSetHttpSessionAttributeOpenmrsMessageToUndiscontinueSuccessfullyAndRedirectToPatientDashboardWhenUndiscontinueOrderWasSuccessfulAndGivenPatientId()
	        throws Exception {
		
		//given
		RadiologyOrder mockRadiologyOrder = RadiologyTestData.getMockRadiologyOrder1();
		Study mockStudyPreSave = RadiologyTestData.getMockStudy1PreSave();
		Study mockStudyPostSave = RadiologyTestData.getMockStudy1PostSave();
		mockStudyPostSave.setMwlStatus(MwlStatus.UNDISCONTINUE_OK);
		
		when(radiologyService.getRadiologyOrderByOrderId(mockRadiologyOrder.getOrderId())).thenReturn(mockRadiologyOrder);
		when(radiologyService.getStudyByOrderId(mockRadiologyOrder.getOrderId())).thenReturn(mockStudyPostSave);
		
		MockHttpServletRequest mockRequest = new MockHttpServletRequest();
		mockRequest.addParameter("undiscontinueOrder", "undiscontinueOrder");
		MockHttpSession mockSession = new MockHttpSession();
		mockRequest.setSession(mockSession);
		
		BindingResult studyErrors = mock(BindingResult.class);
		when(studyErrors.hasErrors()).thenReturn(false);
		BindingResult orderErrors = mock(BindingResult.class);
		when(orderErrors.hasErrors()).thenReturn(false);
		
		ModelAndView modelAndView = radiologyOrderFormController.post(mockRequest, mockRadiologyOrder.getPatient()
		        .getPatientId(), mockStudyPreSave, studyErrors, mockRadiologyOrder, orderErrors);
		
		assertNotNull(modelAndView);
		assertThat(modelAndView.getViewName(), is("redirect:/patientDashboard.form?patientId="
		        + mockRadiologyOrder.getPatient().getPatientId()));
		assertThat((String) mockSession.getAttribute(WebConstants.OPENMRS_MSG_ATTR), is("Order.undiscontinuedSuccessfully"));
	}
	
	/**
	 * @see RadiologyOrderFormController#isUserReferringPhysician()
	 */
	@Test
	@Verifies(value = "should return true if the current user is authenticated as a referring physician", method = "isUserReferringPhysician()")
	public void isUserReferringPhysician_ShouldReturnTrueIfTheCurrentUserIsAuthenticatedAsAReferringPhysician()
	        throws Exception {
		
		User referringPhysician = RadiologyTestData.getMockRadiologyReferringPhysician();
		when(Context.getAuthenticatedUser()).thenReturn(referringPhysician);
		
		Method isUserReferringPhysicianMethod = radiologyOrderFormController.getClass().getDeclaredMethod(
		    "isUserReferringPhysician", new Class[] {});
		isUserReferringPhysicianMethod.setAccessible(true);
		
		Boolean isUserReferringPhysician = (Boolean) isUserReferringPhysicianMethod.invoke(radiologyOrderFormController,
		    new Object[] {});
		
		assertThat(isUserReferringPhysician, is(true));
	}
	
	/**
	 * @see RadiologyOrderFormController#isUserReferringPhysician()
	 */
	@Test
	@Verifies(value = "should return false if the current user is not authenticated as a referring physician", method = "isUserReferringPhysician()")
	public void isUserReferringPhysician_ShouldReturnFalseIfTheCurrentUserIsNotAuthenticatedAsAReferringPhysician()
	        throws Exception {
		
		when(Context.getAuthenticatedUser()).thenReturn(RadiologyTestData.getMockRadiologyReadingPhysician());
		
		Method isUserReferringPhysicianMethod = radiologyOrderFormController.getClass().getDeclaredMethod(
		    "isUserReferringPhysician", new Class[] {});
		isUserReferringPhysicianMethod.setAccessible(true);
		
		Boolean isUserReferringPhysician = (Boolean) isUserReferringPhysicianMethod.invoke(radiologyOrderFormController,
		    new Object[] {});
		
		assertThat(isUserReferringPhysician, is(false));
	}
	
	/**
	 * @see RadiologyOrderFormController#isUserReferringPhysician()
	 */
	@Test(expected = APIAuthenticationException.class)
	@Verifies(value = "should throw api authentication exception if the current user is not authenticated", method = "isUserReferringPhysician()")
	public void isUserReferringPhysician_ShouldThrowApiAuthenticationExceptionIfTheCurrentUserIsNotAuthenticated()
	        throws Exception {
		
		when(Context.getAuthenticatedUser()).thenReturn(null);
		
		Method isUserReferringPhysicianMethod = radiologyOrderFormController.getClass().getDeclaredMethod(
		    "isUserReferringPhysician", new Class[] {});
		isUserReferringPhysicianMethod.setAccessible(true);
		
		isUserReferringPhysicianMethod.invoke(radiologyOrderFormController, new Object[] {});
	}
	
	/**
	 * @see RadiologyOrderFormController#isUserScheduler()
	 */
	@Test
	@Verifies(value = "should return true if the current user is authenticated as a scheduler", method = "isUserScheduler()")
	public void isUserScheduler_ShouldReturnTrueIfTheCurrentUserIsAuthenticatedAsAScheduler() throws Exception {
		
		User Scheduler = RadiologyTestData.getMockRadiologyScheduler();
		when(Context.getAuthenticatedUser()).thenReturn(Scheduler);
		
		Method isUserSchedulerMethod = radiologyOrderFormController.getClass().getDeclaredMethod("isUserScheduler",
		    new Class[] {});
		isUserSchedulerMethod.setAccessible(true);
		
		Boolean isUserScheduler = (Boolean) isUserSchedulerMethod.invoke(radiologyOrderFormController, new Object[] {});
		
		assertThat(isUserScheduler, is(true));
	}
	
	/**
	 * @see RadiologyOrderFormController#isUserScheduler()
	 */
	@Test
	@Verifies(value = "should return false if the current user is not authenticated as a scheduler", method = "isUserScheduler()")
	public void isUserScheduler_ShouldReturnFalseIfTheCurrentUserIsNotAuthenticatedAsAScheduler() throws Exception {
		
		when(Context.getAuthenticatedUser()).thenReturn(RadiologyTestData.getMockRadiologyReferringPhysician());
		
		Method isUserSchedulerMethod = radiologyOrderFormController.getClass().getDeclaredMethod("isUserScheduler",
		    new Class[] {});
		isUserSchedulerMethod.setAccessible(true);
		
		Boolean isUserScheduler = (Boolean) isUserSchedulerMethod.invoke(radiologyOrderFormController, new Object[] {});
		
		assertThat(isUserScheduler, is(false));
	}
	
	/**
	 * @see RadiologyOrderFormController#isUserScheduler()
	 */
	@Test(expected = APIAuthenticationException.class)
	@Verifies(value = "should throw api authentication exception if the current user is not authenticated", method = "isUserScheduler()")
	public void isUserScheduler_ShouldThrowApiAuthenticationExceptionIfTheCurrentUserIsNotAuthenticated() throws Exception {
		
		when(Context.getAuthenticatedUser()).thenReturn(null);
		
		Method isUserSchedulerMethod = radiologyOrderFormController.getClass().getDeclaredMethod("isUserScheduler",
		    new Class[] {});
		isUserSchedulerMethod.setAccessible(true);
		
		isUserSchedulerMethod.invoke(radiologyOrderFormController, new Object[] {});
	}
	
	/**
	 * @see RadiologyOrderFormController#isUserPerformingPhysician()
	 */
	@Test
	@Verifies(value = "should return true if the current user is authenticated as a Performing physician", method = "isUserPerformingPhysician()")
	public void isUserPerformingPhysician_ShouldReturnTrueIfTheCurrentUserIsAuthenticatedAsAPerformingPhysician()
	        throws Exception {
		
		User PerformingPhysician = RadiologyTestData.getMockRadiologyPerformingPhysician();
		when(Context.getAuthenticatedUser()).thenReturn(PerformingPhysician);
		
		Method isUserPerformingPhysicianMethod = radiologyOrderFormController.getClass().getDeclaredMethod(
		    "isUserPerformingPhysician", new Class[] {});
		isUserPerformingPhysicianMethod.setAccessible(true);
		
		Boolean isUserPerformingPhysician = (Boolean) isUserPerformingPhysicianMethod.invoke(radiologyOrderFormController,
		    new Object[] {});
		
		assertThat(isUserPerformingPhysician, is(true));
	}
	
	/**
	 * @see RadiologyOrderFormController#isUserPerformingPhysician()
	 */
	@Test
	@Verifies(value = "should return false if the current user is not authenticated as a Performing physician", method = "isUserPerformingPhysician()")
	public void isUserPerformingPhysician_ShouldReturnFalseIfTheCurrentUserIsNotAuthenticatedAsAPerformingPhysician()
	        throws Exception {
		
		when(Context.getAuthenticatedUser()).thenReturn(RadiologyTestData.getMockRadiologyReadingPhysician());
		
		Method isUserPerformingPhysicianMethod = radiologyOrderFormController.getClass().getDeclaredMethod(
		    "isUserPerformingPhysician", new Class[] {});
		isUserPerformingPhysicianMethod.setAccessible(true);
		
		Boolean isUserPerformingPhysician = (Boolean) isUserPerformingPhysicianMethod.invoke(radiologyOrderFormController,
		    new Object[] {});
		
		assertThat(isUserPerformingPhysician, is(false));
	}
	
	/**
	 * @see RadiologyOrderFormController#isUserPerformingPhysician()
	 */
	@Test(expected = APIAuthenticationException.class)
	@Verifies(value = "should throw api authentication exception if the current user is not authenticated", method = "isUserPerformingPhysician()")
	public void isUserPerformingPhysician_ShouldThrowApiAuthenticationExceptionTheCurrentUserIsNotAuthenticated()
	        throws Exception {
		
		when(Context.getAuthenticatedUser()).thenReturn(null);
		
		Method isUserPerformingPhysicianMethod = radiologyOrderFormController.getClass().getDeclaredMethod(
		    "isUserPerformingPhysician", new Class[] {});
		isUserPerformingPhysicianMethod.setAccessible(true);
		
		isUserPerformingPhysicianMethod.invoke(radiologyOrderFormController, new Object[] {});
	}
	
	/**
	 * @see RadiologyOrderFormController#isUserReadingPhysician()
	 */
	@Test
	@Verifies(value = "should return true if the current user is authenticated as a Reading physician", method = "isUserReadingPhysician()")
	public void isUserReadingPhysician_ShouldReturnTrueIfTheCurrentUserIsAuthenticatedAsAReadingPhysician() throws Exception {
		
		User ReadingPhysician = RadiologyTestData.getMockRadiologyReadingPhysician();
		when(Context.getAuthenticatedUser()).thenReturn(ReadingPhysician);
		
		Method isUserReadingPhysicianMethod = radiologyOrderFormController.getClass().getDeclaredMethod(
		    "isUserReadingPhysician", new Class[] {});
		isUserReadingPhysicianMethod.setAccessible(true);
		
		isUserReadingPhysicianMethod.invoke(radiologyOrderFormController, new Object[] {});
	}
	
	/**
	 * @see RadiologyOrderFormController#isUserReadingPhysician()
	 */
	@Test
	@Verifies(value = "should return false if the current user is not authenticated as a Reading physician", method = "isUserReadingPhysician()")
	public void isUserReadingPhysician_ShouldReturnFalseIfTheCurrentUserIsNotAuthenticatedAsAReadingPhysician()
	        throws Exception {
		
		when(Context.getAuthenticatedUser()).thenReturn(RadiologyTestData.getMockRadiologyReferringPhysician());
		
		Method isUserReadingPhysicianMethod = radiologyOrderFormController.getClass().getDeclaredMethod(
		    "isUserReadingPhysician", new Class[] {});
		isUserReadingPhysicianMethod.setAccessible(true);
		
		Boolean isUserReadingPhysician = (Boolean) isUserReadingPhysicianMethod.invoke(radiologyOrderFormController,
		    new Object[] {});
		
		assertThat(isUserReadingPhysician, is(false));
	}
	
	/**
	 * @see RadiologyOrderFormController#isUserReadingPhysician()
	 */
	@Test(expected = APIAuthenticationException.class)
	@Verifies(value = "should throw api authentication exception if the current user is not authenticated", method = "isUserReadingPhysician()")
	public void isUserReadingPhysician_ShouldThrowApiAuthenticationExceptionIfTheCurrentUserIsNotAuthenticated()
	        throws Exception {
		when(Context.getAuthenticatedUser()).thenReturn(null);
		
		Method isUserReadingPhysicianMethod = radiologyOrderFormController.getClass().getDeclaredMethod(
		    "isUserReadingPhysician", new Class[] {});
		isUserReadingPhysicianMethod.setAccessible(true);
		
		isUserReadingPhysicianMethod.invoke(radiologyOrderFormController, new Object[] {});
	}
	
	/**
	 * @see RadiologyOrderFormController#isUserSuper()
	 */
	@Test
	@Verifies(value = "should return true if the current user is authenticated as a super user", method = "isUserSuper()")
	public void isUserSuper_ShouldReturnTrueIfTheCurrentUserIsAuthenticatedAsASuperUser() throws Exception {
		
		User Super = RadiologyTestData.getMockRadiologySuperUser();
		when(Context.getAuthenticatedUser()).thenReturn(Super);
		
		Method isUserSuperMethod = radiologyOrderFormController.getClass().getDeclaredMethod("isUserSuper", new Class[] {});
		isUserSuperMethod.setAccessible(true);
		
		Boolean isUserSuper = (Boolean) isUserSuperMethod.invoke(radiologyOrderFormController, new Object[] {});
		
		assertThat(isUserSuper, is(true));
	}
	
	/**
	 * @see RadiologyOrderFormController#isUserSuper()
	 */
	@Test
	@Verifies(value = "should return false if the current user is not authenticated as a super user", method = "isUserSuper()")
	public void isUserSuper_ShouldReturnFalseIfTheCurrentUserIsNotAuthenticatedAsASuperUser() throws Exception {
		
		when(Context.getAuthenticatedUser()).thenReturn(RadiologyTestData.getMockRadiologyReferringPhysician());
		
		Method isUserSuperMethod = radiologyOrderFormController.getClass().getDeclaredMethod("isUserSuper", new Class[] {});
		isUserSuperMethod.setAccessible(true);
		
		Boolean isUserSuper = (Boolean) isUserSuperMethod.invoke(radiologyOrderFormController, new Object[] {});
		
		assertThat(isUserSuper, is(false));
	}
	
	/**
	 * @see RadiologyOrderFormController#isUserSuper()
	 */
	@Test(expected = APIAuthenticationException.class)
	@Verifies(value = "should throw api authentication exception if the current user is not authenticated", method = "isUserSuper()")
	public void isUserSuper_ShouldThrowApiAuthenticationExceptionIfTheCurrentUserIsNotAuthenticated() throws Exception {
		
		when(Context.getAuthenticatedUser()).thenReturn(null);
		
		Method isUserSuperMethod = radiologyOrderFormController.getClass().getDeclaredMethod("isUserSuper", new Class[] {});
		isUserSuperMethod.setAccessible(true);
		
		isUserSuperMethod.invoke(radiologyOrderFormController, new Object[] {});
	}
}
