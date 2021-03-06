/* -*- Mode: C++; tab-width: 20; indent-tabs-mode: nil; c-basic-offset: 2 -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

#ifndef DEVICE_DELEGATE_SVR_DOT_H
#define DEVICE_DELEGATE_SVR_DOT_H

#include "vrb/Forward.h"
#include "vrb/MacroUtils.h"
#include "DeviceDelegate.h"
#include <memory>

class android_app;
namespace crow {

class BrowserEGLContext;

class DeviceDelegateSVR;
typedef std::shared_ptr<DeviceDelegateSVR> DeviceDelegateSVRPtr;

class DeviceDelegateSVR : public DeviceDelegate {
public:
  static DeviceDelegateSVRPtr Create(vrb::RenderContextPtr& aContext, android_app* aApp);
  // DeviceDelegate interface
  void SetRenderMode(const device::RenderMode aMode) override;
  device::RenderMode GetRenderMode() override;
  void RegisterImmersiveDisplay(ImmersiveDisplayPtr aDisplay) override;
  GestureDelegateConstPtr GetGestureDelegate() override { return nullptr; }
  vrb::CameraPtr GetCamera(const device::Eye aWhich) override;
  const vrb::Matrix& GetHeadTransform() const override;
  const vrb::Matrix& GetReorientTransform() const override;
  void SetClearColor(const vrb::Color& aColor) override;
  void SetClipPlanes(const float aNear, const float aFar) override;
  void SetControllerDelegate(ControllerDelegatePtr& aController) override;
  void ReleaseControllerDelegate() override;
  int32_t GetControllerModelCount() const override;
  const std::string GetControllerModelName(const int32_t aModelIndex) const override;
  void ProcessEvents() override;
  void StartFrame() override;
  void BindEye(const device::Eye aWhich) override;
  void EndFrame(const bool aDiscard) override;
  // Custom methods for NativeActivity render loop based devices.
  void EnterVR(const crow::BrowserEGLContext& aEGLContext);
  void LeaveVR();
  bool IsInVRMode() const;
  bool ExitApp();
  void UpdateButtonState(int32_t aWhichButton, bool pressed);
  void UpdateTrackpad(float x, float y);
  void WheelScroll(float speed);
protected:
  struct State;
  DeviceDelegateSVR(State& aState);
  virtual ~DeviceDelegateSVR();
private:
  State& m;
  VRB_NO_DEFAULTS(DeviceDelegateSVR)
};

} // namespace crow

#endif // DEVICE_DELEGATE_SVR_DOT_H
