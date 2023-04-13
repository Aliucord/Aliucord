#pragma once

#include <jni.h>

#include <string_view>

/// \namespace lsplant
namespace lsplant {

inline namespace v2 {
/// \struct InitInfo
/// \brief Information and configuration that are needed to call #Init()
struct InitInfo {
    /// \brief Type of inline hook function.
    /// In \ref std::function form so that user can use lambda expression with capture list.<br>
    /// \p target is the target function to be hooked.<br>
    /// \p hooker is the hooker function to replace the \p target function.<br>
    /// \p return is the backup function that points to the previous target function.
    /// it should return null if hook fails and nonnull if successes.
    using InlineHookFunType = std::function<void *(void *target, void *hooker)>;
    /// \brief Type of inline unhook function.
    /// In \ref std::function form so that user can use lambda expression with capture list.<br>
    /// \p func is the target function that is previously hooked.<br>
    /// \p return should indicate the status of unhooking.<br>
    using InlineUnhookFunType = std::function<bool(void *func)>;
    /// \brief Type of symbol resolver to \p libart.so.
    /// In \ref std::function form so that user can use lambda expression with capture list.<br>
    /// \p symbol_name is the symbol name that needs to retrieve.<br>
    /// \p return is the absolute address in the memory that points to the target symbol. It should
    /// be null if the symbol cannot be found. <br>
    /// \note It should be able to resolve symbols from both .dynsym and .symtab.
    using ArtSymbolResolver = std::function<void *(std::string_view symbol_name)>;

    /// \brief Type of prefix symbol resolver to \p libart.so.
    /// In \ref std::function form so that user can use lambda expression with capture list.<br>
    /// \p symbol_prefix is the symbol prefix that needs to retrieve.<br>
    /// \p return is the first absolute address in the memory that points to the target symbol.
    /// It should be null if the symbol cannot be found. <br>
    /// \note It should be able to resolve symbols from both .dynsym and .symtab.
    using ArtSymbolPrefixResolver = std::function<void *(std::string_view symbol_prefix)>;

    /// \brief The inline hooker function. Must not be null.
    InlineHookFunType inline_hooker;
    /// \brief The inline unhooker function. Must not be null.
    InlineUnhookFunType inline_unhooker;
    /// \brief The symbol resolver to \p libart.so. Must not be null.
    ArtSymbolResolver art_symbol_resolver;

    /// \brief The symbol prefix resolver to \p libart.so. May be null.
    ArtSymbolPrefixResolver art_symbol_prefix_resolver;

    /// \brief The generated class name. Must not be empty. It contains a field and a method
    /// and they could be set by \p generated_field_name and \p generated_method_name respectively.
    std::string_view generated_class_name = "LSPHooker_";
    /// \brief The generated source name. Could be empty.
    std::string_view generated_source_name = "LSP";
    /// \brief The generated field name. Must not be empty.
    std::string_view generated_field_name = "hooker";
    /// \brief The generated class name. Must not be emtpy. If {target} is set,
    /// it will follows the name of the target.
    std::string_view generated_method_name = "{target}";
};

/// \brief Initialize LSPlant for the proceeding hook.
/// It mainly prefetch needed symbols and hook some functions.
/// The env should not have any restriction for accessing hidden APIs.
/// You can obtain such a \ref JNIEnv in JNI_OnLoad().
/// \param[in] env The Java environment. Must not be null.
/// \param[in] info The information for initialized.
/// Basically, the info provides the inline hooker and unhooker together with a symbol resolver of
/// libart.so to hook and extract needed native functions of ART.
/// \return Indicate whether initialization succeed. Behavior is undefined if calling other
/// LSPlant interfaces before initialization or after a fail initialization.
/// \see InitInfo.
[[nodiscard, maybe_unused, gnu::visibility("default")]] bool Init(JNIEnv *env,
                                                                  const InitInfo &info);

/// \brief Hook a Java method by providing the \p target_method together with the context object
/// \p hooker_object and its callback \p callback_method.
/// \param[in] env The Java environment. Must not be null.
/// \param[in] target_method The method id to the method you want to hook. Must not be null.
/// \param[in] hooker_object The hooker object to store the context of the hook.
/// The most likely usage is to store the \b backup method into it so that when \b callback_method
/// is invoked, it can call the original method. Another scenario is that, for example,
/// in Xposed framework, multiple modules can hook the same Java method and the \b hooker_object
/// can be used to store all the callbacks to allow multiple modules work simultaneously without
/// conflict.
/// \param[in] callback_method The callback method to the \p hooker_object is used to replace the
/// \p target_method. Whenever the \p target_method is invoked, the \p callback_method will be
/// invoked instead of the original \p target_method. The signature of the \p callback_method must
/// be:
/// \code{.java}
/// public Object callback_method(Object []args)
/// \endcode
/// That is, the return type must be \p Object and the parameter type must be \b Object[]. Behavior
/// is undefined if the signature does not match the requirement.
/// args[0] is the this object for non-static methods and there is NOT null this object placeholder
/// for static methods.
/// Extra info can be provided by defining member variables of \p hooker_object.
/// This method must be a method to \p hooker_object.
/// \return The backup method. You can invoke it
/// by reflection to invoke the original method. null if fails.
/// \note This function will
/// automatically generate a stub class for hook. To help debug, you can set the generated class
/// name, its field name, its source name and its method name by setting generated_* in \ref
/// InitInfo.
/// \note This function thread safe (you can call it simultaneously from multiple thread)
/// but it's not atomic to the same \b target_method. That means #UnHook() or #IsHooked() does not
/// guarantee to work properly on the same \p target_method before it returns. Also, simultaneously
/// call on this function with the same \p target_method does not guarantee only one will success.
/// If you call this with different \p hooker_object on the same target_method simultaneously, the
/// behavior is undefined.
/// \note The behavior of getting the \ref jmethodID of the backup method is undfined.
[[nodiscard, maybe_unused, gnu::visibility("default")]] jobject Hook(JNIEnv *env,
                                                                     jobject target_method,
                                                                     jobject hooker_object,
                                                                     jobject callback_method);

/// \brief Unhook a Java function that is previously hooked.
/// \param[in] env The Java environment.
/// \param[in] target_method The target method that is previously hooked.
/// \return Indicate whether the unhook succeed.
/// \note Calling \p backup (the return method of #Hook()) after unhooking is undefined behavior.
/// Please read #Hook()'s note for more details.
/// \see Hook()
[[nodiscard, maybe_unused, gnu::visibility("default")]] bool UnHook(JNIEnv *env,
                                                                    jobject target_method);

/// \brief Check if a Java function is hooked by LSPlant or not
/// \param[in] env The Java environment.
/// \param[in] method The method to check if it was hooked or not.
/// \return If \p method hooked, ture; otherwise, false.
/// Please read #Hook()'s note for more details.
/// \see Hook()
[[nodiscard, maybe_unused, gnu::visibility("default")]] bool IsHooked(JNIEnv *env, jobject method);

/// \brief Deoptimize a method to avoid hooked callee not being called because of inline
/// \param[in] env The Java environment.
/// \param[in] method The method to deoptimize. By deoptimizing the method, the method will back all
/// callee without inlining. For example, if you hooked a short method B that is invoked by method
/// A, and you find that your callback to B is not invoked after hooking, then it may mean A has
/// inlined B inside its method body. To force A to call your hooked B, you can deoptimize A and
/// then your hook can take effect. Generally, you need to find all the callers of your hooked
/// callee and that can be hardly achieve (but you can still search all callers by using DexHelper).
/// Use this function if you are sure the deoptimized callers
/// are all you need. Otherwise, it would be better to change the hook point or to deoptimize the
/// whole app manually (by simple reinstall the app without uninstalled).
/// \return Indicate whether the deoptimizing succeed or not.
/// \note It is safe to call deoptimizing on a hooked method because the deoptimization will
/// perform on the backup method instead.
[[nodiscard, maybe_unused, gnu::visibility("default")]] bool Deoptimize(JNIEnv *env,
                                                                        jobject method);

/// \brief Get the registered native function pointer of a native function. It helps user to hook
/// native methods directly by backing up the native function pointer this function returns and
/// env->registerNatives another native function pointer.
/// \param[in] env The Java environment.
/// \param[in] method The native method to get the native function pointer.
/// \return The native function pointer the \p method previously registered. If it has not been
/// registered or it is not a native method, null is returned instead.
[[nodiscard, maybe_unused, gnu::visibility("default")]] void *GetNativeFunction(JNIEnv *env,
                                                                                jobject method);

/// \brief Make a class inheritable. It will make the class non-final and make all its private
/// constructors protected.
/// \param[in] env The Java environment.
/// \param[in] target The target class that is to make inheritable.
/// \return Indicate whether the operation has succeed.
[[nodiscard, maybe_unused, gnu::visibility("default")]] bool MakeClassInheritable(JNIEnv *env,
                                                                                  jclass target);

/// \brief Make a DexFile trustable so that it can access hidden APIs. This is useful because we
/// likely need to access hidden APIs when hooking system methods. A concern of this function is
/// that cookie of the DexFile maybe a hidden APIs. So get please get the needed \ref jfieldID
/// beforehand (like in JNI_OnLoad as #Init()).
/// \param[in] env The Java environment.
/// \param[in] cookie The cookie of the DexFile.
/// \return Indicate whether the operation has succeed.
[[nodiscard, maybe_unused, gnu::visibility("default")]] bool MakeDexFileTrusted(JNIEnv *env,
                                                                                jobject cookie);
}  // namespace v1
}  // namespace lsplant
